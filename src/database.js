const Database = require('better-sqlite3');
const path = require('path');

const DB_PATH = path.join(__dirname, '..', 'data', 'calls.db');

let db;

function getDb() {
  if (!db) {
    const fs = require('fs');
    const dataDir = path.dirname(DB_PATH);
    if (!fs.existsSync(dataDir)) {
      fs.mkdirSync(dataDir, { recursive: true });
    }

    db = new Database(DB_PATH);
    db.pragma('journal_mode = WAL');
    db.pragma('foreign_keys = ON');

    db.exec(`
      CREATE TABLE IF NOT EXISTS call_logs (
        id INTEGER PRIMARY KEY AUTOINCREMENT,
        caller_number TEXT NOT NULL,
        call_date TEXT NOT NULL DEFAULT (datetime('now', 'localtime')),
        dnis TEXT,
        session_id TEXT
      );

      CREATE INDEX IF NOT EXISTS idx_caller_number ON call_logs(caller_number);
    `);
  }
  return db;
}

/**
 * Arayan numaranın daha önce arayıp aramadığını kontrol eder.
 * Eğer ilk kez arıyorsa true, daha önce aramışsa false döner.
 */
function isNewCaller(callerNumber) {
  const db = getDb();
  const row = db.prepare('SELECT COUNT(*) as count FROM call_logs WHERE caller_number = ?').get(callerNumber);
  return row.count === 0;
}

/**
 * Çağrıyı veritabanına kaydeder.
 */
function logCall(callerNumber, dnis, sessionId) {
  const db = getDb();
  const stmt = db.prepare('INSERT INTO call_logs (caller_number, dnis, session_id) VALUES (?, ?, ?)');
  return stmt.run(callerNumber, dnis || null, sessionId || null);
}

/**
 * Bir numaraya ait tüm çağrı kayıtlarını getirir.
 */
function getCallHistory(callerNumber) {
  const db = getDb();
  return db.prepare('SELECT * FROM call_logs WHERE caller_number = ? ORDER BY call_date DESC').all(callerNumber);
}

/**
 * Tüm çağrı kayıtlarını getirir (sayfalama ile).
 */
function getAllCalls(limit = 100, offset = 0) {
  const db = getDb();
  return db.prepare('SELECT * FROM call_logs ORDER BY call_date DESC LIMIT ? OFFSET ?').all(limit, offset);
}

function closeDb() {
  if (db) {
    db.close();
    db = null;
  }
}

module.exports = {
  getDb,
  isNewCaller,
  logCall,
  getCallHistory,
  getAllCalls,
  closeDb
};
