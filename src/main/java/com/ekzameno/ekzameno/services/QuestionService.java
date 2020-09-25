package com.ekzameno.ekzameno.services;

import java.sql.SQLException;
import java.util.List;
import java.util.UUID;

import javax.ws.rs.BadRequestException;
import javax.ws.rs.InternalServerErrorException;

import com.ekzameno.ekzameno.dtos.CreateAnswerDTO;
import com.ekzameno.ekzameno.models.Answer;
import com.ekzameno.ekzameno.models.MultipleChoiceQuestion;
import com.ekzameno.ekzameno.models.Question;
import com.ekzameno.ekzameno.models.ShortAnswerQuestion;
import com.ekzameno.ekzameno.shared.DBConnection;

/**
 * Service to handle questions.
 */
public class QuestionService {
    /**
     * Create a question for a given exam.
     *
     * @param examId   ID of the exam to create the question for
     * @param question text of the question
     * @param marks    number of marks allocated to the question
     * @param type     type of the question
     * @param answers  answers for the question
     * @return created Question
     */
    public Question createQuestion(
        UUID examId,
        String question,
        int marks,
        String type,
        List<CreateAnswerDTO> answers
    ) {
        try (DBConnection connection = DBConnection.getInstance()) {
            Question q;

            if (type.toUpperCase().equals(MultipleChoiceQuestion.TYPE)) {
                q = new MultipleChoiceQuestion(question, marks, examId);

                for (CreateAnswerDTO a : answers) {
                    new Answer(a.answer, a.correct, q.getId());
                }

            } else if (type.toUpperCase().equals(ShortAnswerQuestion.TYPE)) {
                q = new ShortAnswerQuestion(question, marks, examId);
            } else {
                throw new BadRequestException();
            }

            return q;
        } catch (SQLException e) {
            e.printStackTrace();
            throw new InternalServerErrorException();
        }
    }
}
