// Service for spontaneous messages from Gerda between 9:00 and 20:00 (~3 times a day)
// and sending system/browser notifications and audio chimes when a message arrives.

import { chatTurn } from "./ai.functions";
import { getGerdaSystemPrompt } from "./useGeminiChat";

export interface SpontaneousSlot {
  id: string;
  targetMinute: number; // minutes from 00:00
  sent: boolean;
  sentAt?: string;
  messageText?: string;
}

export interface SpontaneousDaySchedule {
  date: string; // YYYY-MM-DD
  slots: SpontaneousSlot[];
}

const STORAGE_KEY_SCHEDULE = "gerda_spontaneous_schedule_v2";
const STORAGE_KEY_ENABLED = "gerda_spontaneous_enabled";

const SPONTANEOUS_FALLBACK_MESSAGES = [
  "hee wat doe je?? ik zit in de mekdonalts met 4 hambuurgers",
  "ben je al wakker?? kom je meknuggits brengen alsjeblieft",
  "ik verveel me dood hier in de mekdonalts kom ook ff langs",
  "kijk ik heb een vette foto gemaakt in de mekdonalts [SEND_PHOTO: foto_macdonalds]",
  "heee waarom app je me niet ben je boos op me ofzo",
  "brendi boterpak zei dat ik te dik word maar ik eet gewoon nog een burger haha",
  "wil je zo meegaan naar de mekdonalts? ik trakteer (grapje jij moet betalen)",
  "me milkshake is omgevallen over me broek heen... echt huilen dit",
  "ik zit hier al vanaf vanmorgen vroeg aan de franse frietjes",
  "leef je nog??? laat eens wat van je horen!",
  "kijk me buik [SEND_VIDEO: video_buikje_slaan]",
  "heb je al gegeten vandaag? ik heb al 6 kipnuggits en een franse friet op",
];

// Play a pleasant two-tone WhatsApp incoming message notification chime
export function playNotificationChime() {
  try {
    const AudioCtx = window.AudioContext || (window as any).webkitAudioContext;
    if (!AudioCtx) return;
    const ctx = new AudioCtx();
    if (ctx.state === "suspended") {
      ctx.resume().catch(() => {});
    }

    const now = ctx.currentTime;
    // Tone 1
    const osc1 = ctx.createOscillator();
    const gain1 = ctx.createGain();
    osc1.type = "sine";
    osc1.frequency.setValueAtTime(880, now); // A5
    gain1.gain.setValueAtTime(0.18, now);
    gain1.gain.exponentialRampToValueAtTime(0.001, now + 0.12);
    osc1.connect(gain1);
    gain1.connect(ctx.destination);
    osc1.start(now);
    osc1.stop(now + 0.14);

    // Tone 2
    const osc2 = ctx.createOscillator();
    const gain2 = ctx.createGain();
    osc2.type = "sine";
    osc2.frequency.setValueAtTime(1318.5, now + 0.08); // E6
    gain2.gain.setValueAtTime(0.22, now + 0.08);
    gain2.gain.exponentialRampToValueAtTime(0.001, now + 0.3);
    osc2.connect(gain2);
    gain2.connect(ctx.destination);
    osc2.start(now + 0.08);
    osc2.stop(now + 0.32);
  } catch (e) {
    console.warn("[chime] Audio playback failed:", e);
  }
}

export function requestNotificationPermission(): Promise<NotificationPermission> {
  if (typeof window === "undefined" || !("Notification" in window)) {
    return Promise.resolve("denied" as NotificationPermission);
  }
  if (Notification.permission === "default") {
    return Notification.requestPermission();
  }
  return Promise.resolve(Notification.permission);
}

export function showSystemNotification(
  title: string,
  body: string,
  icon = "https://i.imgur.com/e9o18Au.jpeg",
  onClick?: () => void,
) {
  if (typeof window === "undefined" || !("Notification" in window)) return;
  if (Notification.permission === "granted") {
    try {
      const notif = new Notification(title, {
        body,
        icon,
        badge: icon,
        tag: "gerda-chat-" + Date.now(),
      });
      if (onClick) {
        notif.onclick = () => {
          window.focus();
          onClick();
          notif.close();
        };
      }
    } catch (e) {
      console.warn("[notification] error:", e);
    }
  }
}

export function isSpontaneousMessagesEnabled(): boolean {
  if (typeof window === "undefined") return true;
  return localStorage.getItem(STORAGE_KEY_ENABLED) !== "false";
}

export function setSpontaneousMessagesEnabled(enabled: boolean): void {
  if (typeof window === "undefined") return;
  localStorage.setItem(STORAGE_KEY_ENABLED, enabled ? "true" : "false");
}

function getTodayString(): string {
  const d = new Date();
  const year = d.getFullYear();
  const month = String(d.getMonth() + 1).padStart(2, "0");
  const day = String(d.getDate()).padStart(2, "0");
  return `${year}-${month}-${day}`;
}

// Generate 3 random minute markers between 9:00 (540 min) and 20:00 (1200 min)
// 3 slots distributed roughly into morning, afternoon, evening:
// Slot 1: 09:15 - 12:30 (555 - 750)
// Slot 2: 12:45 - 16:15 (765 - 975)
// Slot 3: 16:30 - 19:45 (990 - 1185)
function generateScheduleForToday(): SpontaneousDaySchedule {
  const randBetween = (min: number, max: number) =>
    Math.floor(Math.random() * (max - min + 1)) + min;

  const date = getTodayString();
  const slot1 = randBetween(555, 750);
  const slot2 = randBetween(765, 975);
  const slot3 = randBetween(990, 1185);

  return {
    date,
    slots: [
      { id: `${date}_1`, targetMinute: slot1, sent: false },
      { id: `${date}_2`, targetMinute: slot2, sent: false },
      { id: `${date}_3`, targetMinute: slot3, sent: false },
    ],
  };
}

export function getTodaySchedule(): SpontaneousDaySchedule {
  const today = getTodayString();
  try {
    const raw = localStorage.getItem(STORAGE_KEY_SCHEDULE);
    if (raw) {
      const parsed = JSON.parse(raw) as SpontaneousDaySchedule;
      if (parsed.date === today && Array.isArray(parsed.slots)) {
        return parsed;
      }
    }
  } catch (e) {}

  const fresh = generateScheduleForToday();
  saveSchedule(fresh);
  return fresh;
}

function saveSchedule(schedule: SpontaneousDaySchedule): void {
  try {
    localStorage.setItem(STORAGE_KEY_SCHEDULE, JSON.stringify(schedule));
  } catch (e) {}
}

export async function generateSpontaneousMessageText(): Promise<string> {
  const randomFallback =
    SPONTANEOUS_FALLBACK_MESSAGES[
      Math.floor(Math.random() * SPONTANEOUS_FALLBACK_MESSAGES.length)
    ];

  try {
    const systemPrompt = getGerdaSystemPrompt();
    const prompt =
      "(Stuur uit jezelf een spontaan, willekeurig en grappig WhatsApp-berichtje naar de gebruiker. Je zit in de mekdonalts, verveelt je, bent hongerig naar hamburgers of vraagt wat de ander doet. Reageer in maximaal 1 of 2 korte zinnen in jouw typische kinderlijke stijl met veel spelfouten.)";

    const res = await chatTurn({
      data: {
        systemPrompt,
        history: [],
        message: prompt,
      },
    });

    if (res?.text && res.text.trim()) {
      return res.text.trim();
    }
  } catch (e) {
    console.warn("[spontaneous] AI generation failed, using fallback:", e);
  }

  return randomFallback;
}

export async function checkAndTriggerDueSpontaneousMessage(
  onMessage: (text: string) => void,
): Promise<boolean> {
  if (!isSpontaneousMessagesEnabled()) return false;

  const now = new Date();
  const hour = now.getHours();
  // Alleen tussen 9:00 en 20:00
  if (hour < 9 || hour >= 20) {
    return false;
  }

  const currentMinute = hour * 60 + now.getMinutes();
  const schedule = getTodaySchedule();

  // Zoek een slot dat nog niet verstuurd is en waarvan het doeltijdstip is verstreken
  const dueSlot = schedule.slots.find(
    (slot) => !slot.sent && currentMinute >= slot.targetMinute,
  );

  if (!dueSlot) {
    return false;
  }

  // Markeer direct als verzonden om dubbele verzending te voorkomen
  dueSlot.sent = true;
  dueSlot.sentAt = now.toLocaleTimeString([], { hour: "2-digit", minute: "2-digit" });
  saveSchedule(schedule);

  const text = await generateSpontaneousMessageText();
  dueSlot.messageText = text;
  saveSchedule(schedule);

  onMessage(text);
  return true;
}
