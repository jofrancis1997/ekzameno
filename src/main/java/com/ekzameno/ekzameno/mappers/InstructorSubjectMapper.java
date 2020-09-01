package com.ekzameno.ekzameno.mappers;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

import com.ekzameno.ekzameno.models.InstructorSubject;
import com.ekzameno.ekzameno.shared.DBConnection;
import com.ekzameno.ekzameno.shared.IdentityMap;

/**
 * Data Mapper for InstructorSubjects.
 */
public class InstructorSubjectMapper extends Mapper<InstructorSubject> {
    private static final String tableName = "instructor_subjects";

    public InstructorSubject findByRelationIds(
        UUID instructorId,
        UUID subjectId
    ) throws SQLException {
        String query = "SELECT * FROM " + tableName +
            " WHERE user_id = ? AND subject_id = ?";

        Connection connection = DBConnection.getInstance().getConnection();

        try (
            PreparedStatement statement = connection.prepareStatement(query);
        ) {
            statement.setObject(1, instructorId);
            statement.setObject(2, subjectId);
            ResultSet rs = statement.executeQuery();
            InstructorSubject instructorSubject = load(rs);
            IdentityMap.getInstance().put(
                instructorSubject.getId(),
                instructorSubject
            );
            return instructorSubject;
        }
    }

    @Override
    public void insert(
        InstructorSubject instructorSubject
    ) throws SQLException {
        String query = "INSERT INTO " + tableName +
            " (id, user_id, subject_id) VALUES (?,?,?)";

        Connection connection = DBConnection.getInstance().getConnection();

        try (
            PreparedStatement statement = connection.prepareStatement(query);
        ) {
            statement.setObject(1, instructorSubject.getId());
            statement.setObject(2, instructorSubject.getInstructorId());
            statement.setObject(3, instructorSubject.getSubjectId());
            statement.executeUpdate();
        }
    }

    @Override
    public void update(
        InstructorSubject instructorSubject
    ) throws SQLException {
        String query = "UPDATE " + tableName +
            " SET user_id = ?, subject_id = ? WHERE id = ?";

        Connection connection = DBConnection.getInstance().getConnection();

        try (
            PreparedStatement statement = connection.prepareStatement(query);
        ) {
            statement.setObject(1, instructorSubject.getInstructorId());
            statement.setObject(1, instructorSubject.getSubjectId());
            statement.setObject(1, instructorSubject.getId());
            statement.executeUpdate();
        }
    }

    public void deleteByRelationIds(
        UUID instructorId,
        UUID subjectId
    ) throws SQLException {
        String query = "DELETE FROM " + tableName +
            " WHERE user_id = ? AND subject_id = ?";

        Connection connection = DBConnection.getInstance().getConnection();

        try (
            PreparedStatement statement = connection.prepareStatement(query);
        ) {
            statement.setObject(1, instructorId);
            statement.setObject(2, subjectId);
            statement.executeUpdate();
        }
    }

    @Override
    protected InstructorSubject load(ResultSet rs) throws SQLException {
        UUID id = rs.getObject("id", java.util.UUID.class);
        UUID instructorId = rs.getObject("user_id", java.util.UUID.class);
        UUID subjectId = rs.getObject("subject_id", java.util.UUID.class);
        return new InstructorSubject(id, instructorId, subjectId);
    }

    @Override
    protected String getTableName() {
        return tableName;
    }
}
