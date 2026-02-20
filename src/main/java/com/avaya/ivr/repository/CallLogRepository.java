package com.avaya.ivr.repository;

import com.avaya.ivr.entity.CallLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CallLogRepository extends JpaRepository<CallLog, Long> {

    boolean existsByCallerNumber(String callerNumber);

    List<CallLog> findByCallerNumberOrderByCallTimeDesc(String callerNumber);

    long countByCallerNumber(String callerNumber);
}
