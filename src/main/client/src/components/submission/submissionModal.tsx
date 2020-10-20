import { unwrapResult } from "@reduxjs/toolkit";
import { Formik } from "formik";
import { FormikControl } from "formik-react-bootstrap";
import React, { useState } from "react";
import { Alert, Button, Form, Modal } from "react-bootstrap";
import { useSelector } from "react-redux";
import { ExamState, QuestionSubmission, selectExamById, submitExam, updateExamSubmission } from "../../redux/slices/examsSlice";
import { QuestionState, selectQuestionsForExam } from "../../redux/slices/questionsSlice";
import { selectMe, selectUserById, UserState } from "../../redux/slices/usersSlice";
import { RootState, useAppDispatch } from "../../redux/store";

interface SubmissionModalProps {
  show: boolean,
  onHide: () => any,
  examId: string,
  studentId: string,
  eTag?: string,
}

interface FormValues {
  marks: number[],
}

export const SubmissionModal = (props: SubmissionModalProps): JSX.Element => {
  const dispatch = useAppDispatch();
  const student = useSelector<RootState, UserState | undefined>(
    state => selectUserById(state, props.studentId)
  );
  const me = useSelector(selectMe);
  const [errorMessage, setErrorMessage] = useState<string | null>(null);

  const handleHide = (): void => {
    setErrorMessage(null);
    props.onHide();
  };

  const questionSubmissions: Record<string, QuestionSubmission | undefined> = {};

  useSelector<RootState, ExamState | undefined>(
    state => selectExamById(state, props.examId)
  )
    ?.submissions
    ?.find(submission => submission.studentId === props.studentId)
    ?.questionSubmissions
    .forEach(questionSubmission => {
      questionSubmissions[questionSubmission.questionId] = questionSubmission;
    });

  const questionMap: Record<string, QuestionState> = {};
  const questions = useSelector(selectQuestionsForExam(props.examId));

  questions
    .forEach(question => {
      questionMap[question.id] = question;
    });

  const handleSubmit = (values: FormValues): Promise<void> => {
    const newMarks = values.marks.reduce((a, b) => a + b, 0);

    if (props.eTag === undefined) {
      return dispatch(submitExam({
        examId: props.examId,
        studentId: props.studentId,
        marks: newMarks,
        answers: [],
      }))
        .then(unwrapResult)
        .then(handleHide)
        .catch((e: Error) => {
          setErrorMessage("Failed to mark submission");
          console.error(e);
        });
    } else {
      return dispatch(updateExamSubmission({
        examId: props.examId,
        studentId: props.studentId,
        marks: newMarks,
        eTag: props.eTag,
      }))
        .then(unwrapResult)
        .then(handleHide)
        .catch((e: Error) => {
          setErrorMessage("Failed to mark submission");
          console.error(e);
        });
    }
  };

  return (
    <Modal show={props.show} onHide={handleHide} centered>
      <Modal.Header closeButton>
        <Modal.Title>
          {
            // eslint-disable-next-line @typescript-eslint/no-non-null-assertion
            student!.name
          }&apos;s Exam
        </Modal.Title>
      </Modal.Header>
      {
        errorMessage !== null &&
          <Alert variant="danger">
            {errorMessage}
          </Alert>
      }
      <Formik
        initialValues={{ marks: [] }}
        onSubmit={handleSubmit}>
        {
          ({
            isSubmitting,
            handleSubmit,
          }) => (
            <Form onSubmit={handleSubmit as any}>
              <fieldset disabled={me?.type === "STUDENT"}>
                <Modal.Body>
                  {
                    questions.map((question, i) => (
                      <React.Fragment key={question.id}>
                        <Form.Group>
                          <Form.Label>{question.question}</Form.Label>
                          <Form.Control
                            type="text"
                            placeholder={questionSubmissions[question.id]?.answer ?? ""}
                            readOnly />
                        </Form.Group>
                        <FormikControl
                          label="Marks"
                          min="0"
                          max={question.marks}
                          name={`marks[${i}]`}
                          type="number"/>
                        <br />
                      </React.Fragment>
                    ))
                  }
                </Modal.Body>
              </fieldset>
              {
                me?.type !== "STUDENT" &&
                  <Modal.Footer>
                    <Button type="submit" disabled={isSubmitting}>Submit Marks</Button>
                  </Modal.Footer>
              }
            </Form>
          )
        }
      </Formik>
    </Modal>
  );
};
