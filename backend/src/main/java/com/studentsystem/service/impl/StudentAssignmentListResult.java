package com.studentsystem.service.impl;

import com.studentsystem.entity.Assignment;
import com.studentsystem.entity.StudentAssignment;

import java.util.List;

public class StudentAssignmentListResult {
    private List<Assignment> pending;      // 待提交
    private List<StudentAssignment> submitted;  // 已提交
    private List<Assignment> expired;      // 已截止

    public List<Assignment> getPending() {
        return pending;
    }

    public void setPending(List<Assignment> pending) {
        this.pending = pending;
    }

    public List<StudentAssignment> getSubmitted() {
        return submitted;
    }

    public void setSubmitted(List<StudentAssignment> submitted) {
        this.submitted = submitted;
    }

    public List<Assignment> getExpired() {
        return expired;
    }

    public void setExpired(List<Assignment> expired) {
        this.expired = expired;
    }
}