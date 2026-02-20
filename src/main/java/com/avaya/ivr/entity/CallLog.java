package com.avaya.ivr.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "call_logs")
public class CallLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "caller_number", nullable = false)
    private String callerNumber;

    @Column(name = "call_time", nullable = false)
    private LocalDateTime callTime;

    @Column(name = "ucid")
    private String ucid;

    @Column(name = "dnis")
    private String dnis;

    public CallLog() {
    }

    public CallLog(String callerNumber, LocalDateTime callTime, String ucid, String dnis) {
        this.callerNumber = callerNumber;
        this.callTime = callTime;
        this.ucid = ucid;
        this.dnis = dnis;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCallerNumber() {
        return callerNumber;
    }

    public void setCallerNumber(String callerNumber) {
        this.callerNumber = callerNumber;
    }

    public LocalDateTime getCallTime() {
        return callTime;
    }

    public void setCallTime(LocalDateTime callTime) {
        this.callTime = callTime;
    }

    public String getUcid() {
        return ucid;
    }

    public void setUcid(String ucid) {
        this.ucid = ucid;
    }

    public String getDnis() {
        return dnis;
    }

    public void setDnis(String dnis) {
        this.dnis = dnis;
    }
}
