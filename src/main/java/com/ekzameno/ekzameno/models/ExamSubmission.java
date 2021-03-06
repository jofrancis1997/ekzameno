package com.ekzameno.ekzameno.models;

import java.sql.SQLException;
import java.util.List;
import java.util.UUID;

import com.ekzameno.ekzameno.mappers.ExamMapper;
import com.ekzameno.ekzameno.mappers.StudentMapper;
import com.ekzameno.ekzameno.proxies.ProxyList;
import com.ekzameno.ekzameno.proxies.QuestionSubmissionExamSubmissionProxyList;
import com.ekzameno.ekzameno.shared.UnitOfWork;
import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * Submission for an Exam.
 */
public class ExamSubmission extends Model {
    private Integer marks;
    private ProxyList<QuestionSubmission> questionSubmissions;
    private UUID studentId;
    private UUID examId;
    @JsonIgnore
    private Student student = null;
    @JsonIgnore
    private Exam exam = null;

    /**
     * Create an ExamSubmission with an ID.
     *
     * @param id        ID of the ExamSubmission
     * @param marks     total number of marks for the ExamSubmission
     * @param studentId ID of the related student
     * @param examId    ID of the related exam
     */
    public ExamSubmission(UUID id, Integer marks, UUID studentId, UUID examId) {
        super(id);
        this.marks = marks;
        this.studentId = studentId;
        this.examId = examId;
        this.questionSubmissions =
            new QuestionSubmissionExamSubmissionProxyList(id);
    }

    /**
     * Create an ExamSubmission without an ID (registers as new).
     *
     * @param marks     total number of marks for the ExamSubmission.
     * @param studentId ID of the related student
     * @param examId    ID of the related exam
     */
    public ExamSubmission(Integer marks, UUID studentId, UUID examId) {
        this.marks = marks;
        this.studentId = studentId;
        this.examId = examId;
        this.questionSubmissions =
            new QuestionSubmissionExamSubmissionProxyList(getId());
    }

    public Integer getMarks() {
        return marks;
    }

    /**
     * Retrieve QuestionSubmissions for the ExamSubmission.
     *
     * @return QuestionSubmissions for the ExamSubmission
     */
    public ProxyList<QuestionSubmission> getQuestionSubmissions() {
        return questionSubmissions;
    }

    @JsonGetter("questionSubmissions")
    private List<QuestionSubmission> getRawQuestionSubmissions()
        throws SQLException {
        return questionSubmissions.getRawList();
    }

    /**
     * Set the number of marks earned by the ExamSubmission (marks the
     * ExamSubmission as dirty).
     *
     * @param marks number of marks earned
     */
    public void setMarks(Integer marks) {
        this.marks = marks;
        UnitOfWork.getCurrent().registerDirty(this);
    }

    public UUID getStudentId() {
        return studentId;
    }

    public UUID getExamId() {
        return examId;
    }

    /**
     * Retrieve related student for this ExamSubmission.
     *
     * @return related student for this ExamSubmission
     * @throws SQLException if unable to retrieve the student
     */
    public Student getStudent() throws SQLException {
        if (student == null) {
            student = new StudentMapper().findById(studentId);
        }
        return student;
    }

    /**
     * Retrieve related exam for this ExamSubmission.
     *
     * @return related exam for this ExamSubmission
     * @throws SQLException if unable to retrieve the exam
     */
    public Exam getExam() throws SQLException {
        if (exam == null) {
            exam = new ExamMapper().findById(examId);
        }
        return exam;
    }

    /**
     * Set the ID of the related student (marks the ExamSubmission as dirty).
     *
     * @param studentId ID of the related student
     */
    public void setStudentId(UUID studentId) {
        this.studentId = studentId;
        this.student = null;
        UnitOfWork.getCurrent().registerDirty(this);
    }

    /**
     * Set the ID of the related exam (marks the ExamSubmission as dirty).
     *
     * @param examId ID of the related exam
     */
    public void setExamId(UUID examId) {
        this.examId = examId;
        this.exam = null;
        UnitOfWork.getCurrent().registerDirty(this);
    }

    /**
     * Set the related student (marks the ExamSubmission as dirty).
     *
     * @param student related student
     */
    public void setStudent(Student student) {
        this.student = student;
        this.studentId = student.getId();
        UnitOfWork.getCurrent().registerDirty(this);
    }

    /**
     * Set the related exam (marks the ExamSubmission as dirty).
     *
     * @param exam related exam
     */
    public void setExam(Exam exam) {
        this.exam = exam;
        this.examId = student.getId();
        UnitOfWork.getCurrent().registerDirty(this);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + ((examId == null) ? 0 : examId.hashCode());
        result = prime * result + ((marks == null) ? 0 : marks.hashCode());
        result = prime * result + ((studentId == null)
            ? 0
            : studentId.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!super.equals(obj)) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        ExamSubmission other = (ExamSubmission) obj;
        if (examId == null) {
            if (other.examId != null) {
                return false;
            }
        } else if (!examId.equals(other.examId)) {
            return false;
        }
        if (marks == null) {
            if (other.marks != null) {
                return false;
            }
        } else if (!marks.equals(other.marks)) {
            return false;
        }
        if (studentId == null) {
            if (other.studentId != null) {
                return false;
            }
        } else if (!studentId.equals(other.studentId)) {
            return false;
        }
        return true;
    }
}
