package com.ekzameno.ekzameno.models;

import java.sql.SQLException;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import com.ekzameno.ekzameno.mappers.ExamSubmissionMapper;
import com.ekzameno.ekzameno.mappers.QuestionMapper;
import com.ekzameno.ekzameno.shared.UnitOfWork;

/**
 * Exam for a Subject.
 */
public class Exam extends Model {
    private String name;
    private DateRange dateRange;
    private List<Question> questions = null;
    private List<ExamSubmission> examSubmissions = null;

    /**
     * Crete an exam with an ID.
     *
     * @param id ID of the exam
     * @param name name of the exam
     * @param dateRange date range of the exam
     */
    public Exam(UUID id, String name, DateRange dateRange) {
        super(id);
        this.name = name;
        this.dateRange = dateRange;
    }

    /**
     * Create an exam without an ID (registers as new).
     *
     * @param name name of the exam
     * @param dateRange date range of the exam
     */
    public Exam(String name, DateRange dateRange) {
        this.name = name;
        this.dateRange = dateRange;
    }

    public String getName() {
        return name;
    }

    public Date getPublishDate() {
        return dateRange.getFromDate();
    }

    public Date getCloseDate() {
        return dateRange.getToDate();
    }

    public List<Question> getQuestions() throws SQLException {
        if (questions == null) {
            return new QuestionMapper().findAllForExam(getId());
        } else {
            return questions;
        }
    }

    public List<ExamSubmission> getExamSubmissions() throws SQLException {
        if (examSubmissions == null) {
            return new ExamSubmissionMapper().findAllForExam(getId());
        } else {
            return examSubmissions;
        }
    }

    public void setName(String name) {
        this.name = name;
        UnitOfWork.getCurrent().registerDirty(this);
    }

    public void setPublishDate(Date publishDate) {
        dateRange.setFromDate(publishDate);
        UnitOfWork.getCurrent().registerDirty(this);
    }

    public void setCloseDate(Date closeDate) {
        dateRange.setToDate(closeDate);
        UnitOfWork.getCurrent().registerDirty(this);
    }
}
