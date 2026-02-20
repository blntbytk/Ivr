package com.avaya.ivr.service;

import com.avaya.ivr.entity.CallLog;
import com.avaya.ivr.repository.CallLogRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class CallLoggingService {

    private static final Logger logger = LoggerFactory.getLogger(CallLoggingService.class);

    private final CallLogRepository callLogRepository;

    public CallLoggingService(CallLogRepository callLogRepository) {
        this.callLogRepository = callLogRepository;
    }

    /**
     * Gelen çağrıyı loglar ve arayanın daha önce arayıp aramadığını kontrol eder.
     *
     * @param callerNumber Arayan numara (ANI/CLI)
     * @param ucid         Avaya UCID
     * @param dnis         Aranan numara (DNIS)
     * @return true ise arayan daha önce aramış (YYY okunmaz), false ise yeni arayan (YYY okunur)
     */
    public boolean processIncomingCall(String callerNumber, String ucid, String dnis) {
        // Arayan daha önce aradı mı kontrol et
        boolean hasCalledBefore = callLogRepository.existsByCallerNumber(callerNumber);

        // Çağrıyı logla
        CallLog callLog = new CallLog(callerNumber, LocalDateTime.now(), ucid, dnis);
        callLogRepository.save(callLog);

        if (hasCalledBefore) {
            logger.info("Arayan {} daha önce aramış. YYY anonsu OKUNMAYACAK. UCID: {}", callerNumber, ucid);
        } else {
            logger.info("Arayan {} ilk kez arıyor. YYY anonsu OKUNACAK. UCID: {}", callerNumber, ucid);
        }

        return hasCalledBefore;
    }

    /**
     * Belirli bir numaranın çağrı geçmişini getirir.
     */
    public List<CallLog> getCallHistory(String callerNumber) {
        return callLogRepository.findByCallerNumberOrderByCallTimeDesc(callerNumber);
    }

    /**
     * Belirli bir numaranın toplam çağrı sayısını getirir.
     */
    public long getCallCount(String callerNumber) {
        return callLogRepository.countByCallerNumber(callerNumber);
    }
}
