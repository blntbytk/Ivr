const express = require('express');
const { isNewCaller, logCall, getCallHistory, getAllCalls, closeDb } = require('./database');

const app = express();
const PORT = process.env.PORT || 3000;

app.use(express.json());
app.use(express.urlencoded({ extended: true }));

/**
 * Ana endpoint: Çağrı geldiğinde Avaya Dialog Designer buraya istek atar.
 *
 * Akış:
 * 1. Arayan numarası (ani) alınır
 * 2. Numara daha önce aramış mı kontrol edilir
 * 3. Çağrı loglara kaydedilir
 * 4. Yeni arayan ise playAnnouncement: true döner (YYY anonsu okutulur)
 *    Daha önce aramışsa playAnnouncement: false döner (YYY anonsu okunmaz)
 */
app.post('/api/call/incoming', (req, res) => {
  const { ani, dnis, sessionId } = req.body;

  if (!ani) {
    return res.status(400).json({
      success: false,
      error: 'ani (arayan numara) parametresi zorunludur'
    });
  }

  const callerIsNew = isNewCaller(ani);

  // Çağrıyı kaydet
  logCall(ani, dnis, sessionId);

  res.json({
    success: true,
    ani,
    isNewCaller: callerIsNew,
    playAnnouncement: callerIsNew,  // true ise YYY anonsu okutulur
    message: callerIsNew
      ? 'Yeni arayan - YYY anonsu okutulacak'
      : 'Bilinen arayan - YYY anonsu okunmayacak'
  });
});

/**
 * GET versiyonu - Dialog Designer web service modülü GET kullanabilir.
 * Örnek: /api/call/incoming?ani=05551234567&dnis=4441234
 */
app.get('/api/call/incoming', (req, res) => {
  const { ani, dnis, sessionId } = req.query;

  if (!ani) {
    return res.status(400).json({
      success: false,
      error: 'ani (arayan numara) parametresi zorunludur'
    });
  }

  const callerIsNew = isNewCaller(ani);

  logCall(ani, dnis, sessionId);

  res.json({
    success: true,
    ani,
    isNewCaller: callerIsNew,
    playAnnouncement: callerIsNew,
    message: callerIsNew
      ? 'Yeni arayan - YYY anonsu okutulacak'
      : 'Bilinen arayan - YYY anonsu okunmayacak'
  });
});

/**
 * Belirli bir numaranın çağrı geçmişini getirir.
 */
app.get('/api/call/history/:ani', (req, res) => {
  const { ani } = req.params;
  const history = getCallHistory(ani);

  res.json({
    success: true,
    ani,
    totalCalls: history.length,
    calls: history
  });
});

/**
 * Tüm çağrı kayıtlarını listeler.
 */
app.get('/api/calls', (req, res) => {
  const limit = parseInt(req.query.limit) || 100;
  const offset = parseInt(req.query.offset) || 0;
  const calls = getAllCalls(limit, offset);

  res.json({
    success: true,
    count: calls.length,
    calls
  });
});

/**
 * Sağlık kontrolü
 */
app.get('/api/health', (req, res) => {
  res.json({ status: 'ok', timestamp: new Date().toISOString() });
});

const server = app.listen(PORT, () => {
  console.log(`Avaya IVR Call Logger - Port ${PORT} üzerinde çalışıyor`);
  console.log(`API endpoint: http://localhost:${PORT}/api/call/incoming`);
});

// Graceful shutdown
process.on('SIGTERM', () => {
  console.log('Sunucu kapatılıyor...');
  closeDb();
  server.close(() => process.exit(0));
});

process.on('SIGINT', () => {
  console.log('Sunucu kapatılıyor...');
  closeDb();
  server.close(() => process.exit(0));
});

module.exports = app;
