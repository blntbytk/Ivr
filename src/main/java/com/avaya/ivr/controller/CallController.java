package com.avaya.ivr.controller;

import com.avaya.ivr.entity.CallLog;
import com.avaya.ivr.service.CallLoggingService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/call")
public class CallController {

    private final CallLoggingService callLoggingService;

    public CallController(CallLoggingService callLoggingService) {
        this.callLoggingService = callLoggingService;
    }

    /**
     * Avaya Dialog Designer'dan gelen çağrı bilgisini alır.
     * Arayan numarayı loglar ve YYY anonsu okunup okunmayacağını döner.
     *
     * Dialog Designer'da Web Service modülü ile bu endpoint çağrılır.
     * ANI (callerNumber) parametresi session:ani değişkeninden gelir.
     *
     * Örnek istek:
     * GET /api/call/incoming?callerNumber=05321234567&ucid=00001234567890&dnis=4441234
     *
     * Örnek yanıt (yeni arayan):
     * {
     *   "callerNumber": "05321234567",
     *   "hasCalledBefore": false,
     *   "playYYY": true,
     *   "message": "Yeni arayan - YYY anonsu okunacak"
     * }
     */
    @GetMapping("/incoming")
    public ResponseEntity<Map<String, Object>> handleIncomingCall(
            @RequestParam String callerNumber,
            @RequestParam(required = false) String ucid,
            @RequestParam(required = false) String dnis) {

        boolean hasCalledBefore = callLoggingService.processIncomingCall(callerNumber, ucid, dnis);

        Map<String, Object> response = new HashMap<>();
        response.put("callerNumber", callerNumber);
        response.put("hasCalledBefore", hasCalledBefore);
        response.put("playYYY", !hasCalledBefore);

        if (hasCalledBefore) {
            response.put("message", "Arayan daha önce aramış - YYY anonsu okunmayacak");
        } else {
            response.put("message", "Yeni arayan - YYY anonsu okunacak");
        }

        return ResponseEntity.ok(response);
    }

    /**
     * POST ile de çağrı bilgisi alınabilir (Dialog Designer tercihine göre).
     *
     * Örnek istek body:
     * {
     *   "callerNumber": "05321234567",
     *   "ucid": "00001234567890",
     *   "dnis": "4441234"
     * }
     */
    @PostMapping("/incoming")
    public ResponseEntity<Map<String, Object>> handleIncomingCallPost(
            @RequestBody Map<String, String> request) {

        String callerNumber = request.get("callerNumber");
        String ucid = request.get("ucid");
        String dnis = request.get("dnis");

        if (callerNumber == null || callerNumber.isBlank()) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", "callerNumber parametresi zorunludur");
            return ResponseEntity.badRequest().body(error);
        }

        boolean hasCalledBefore = callLoggingService.processIncomingCall(callerNumber, ucid, dnis);

        Map<String, Object> response = new HashMap<>();
        response.put("callerNumber", callerNumber);
        response.put("hasCalledBefore", hasCalledBefore);
        response.put("playYYY", !hasCalledBefore);

        if (hasCalledBefore) {
            response.put("message", "Arayan daha önce aramış - YYY anonsu okunmayacak");
        } else {
            response.put("message", "Yeni arayan - YYY anonsu okunacak");
        }

        return ResponseEntity.ok(response);
    }

    /**
     * Belirli bir numaranın çağrı geçmişini getirir.
     */
    @GetMapping("/history/{callerNumber}")
    public ResponseEntity<Map<String, Object>> getCallHistory(
            @PathVariable String callerNumber) {

        List<CallLog> history = callLoggingService.getCallHistory(callerNumber);
        long totalCalls = callLoggingService.getCallCount(callerNumber);

        Map<String, Object> response = new HashMap<>();
        response.put("callerNumber", callerNumber);
        response.put("totalCalls", totalCalls);
        response.put("history", history);

        return ResponseEntity.ok(response);
    }
}
