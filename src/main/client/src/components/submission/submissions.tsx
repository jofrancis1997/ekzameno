import { unwrapResult } from "@reduxjs/toolkit";
import { Formik } from "formik";
import React, { Fragment, useEffect, useState } from "react";
import { Button, Form, Table } from "react-bootstrap";
import { useSelector } from "react-redux";
import * as yup from "yup";
import { ExamState, selectExamById } from "../../redux/slices/examsSlice";
import { createExamSubmission, ExamSubmissionState, fetchExamSubmissions, selectExamSubmissionsForExam, updateExamSubmission } from "../../redux/slices/examSubmissionsSlice";
import { fetchQuestions, selectQuestionsForExam } from "../../redux/slices/questionsSlice";
import { selectSubjectById, SubjectState } from "../../redux/slices/subjectsSlice";
import { fetchUsers, selectMe, selectUsersByIds, selectUsersStatus } from "../../redux/slices/usersSlice";
import { RootState, useAppDispatch } from "../../redux/store";
import { Loader } from "../loader/loader";
import { SubmissionModal } from "./submissionModal";

export interface SubmissionsProps {
  examId: string,
}

interface FormValues {
  marks: {
    studentId: string,
    marks?: number,
  }[],
}

const FormSchema = yup.object().shape({
  marks: yup.array().of(yup.object().shape({
    studentId: yup.string().required(),
    marks: yup.number(),
  })).required("Marks is a required field."),
});

export const Submissions = (props: SubmissionsProps): JSX.Element => {
  const dispatch = useAppDispatch();
  const usersStatus = useSelector(selectUsersStatus);
  const me = useSelector(selectMe);
  const exam = useSelector<RootState, ExamState | undefined>(
    state => selectExamById(state, props.examId)
  );
  const examSubmissions = useSelector(selectExamSubmissionsForExam(exam?.id));
  const questions = useSelector(selectQuestionsForExam(props.examId));
  const subject = useSelector<RootState, SubjectState | undefined>(
    state => selectSubjectById(state, exam?.subjectId ?? "")
  );
  const [examSubmissionsLoading, setExamSubmissionsLoading] = useState(true);
  const [questionsLoading, setQuestionsLoading] = useState(true);

  let students = useSelector(selectUsersByIds(subject?.students ?? []));

  const [
    submissionModalShow,
    setSubmissionModalShow,
  ] = useState<string | null>(null);

  if (me?.type === "STUDENT") {
    students = students.filter(s => s.id === me.id);
  }

  const submissions: Record<string, ExamSubmissionState | undefined> = {};

  examSubmissions.forEach(submission => {
    if (submission.marks !== undefined) {
      submissions[submission.studentId] = submission;
    }
  });

  useEffect(() => {
    dispatch(fetchExamSubmissions(props.examId))
      .then(unwrapResult)
      .then(() => {
        setExamSubmissionsLoading(false);
      })
      .catch(e => {
        console.error(e);
      });
  }, [dispatch, props.examId]);

  useEffect(() => {
    if (usersStatus === "idle" && me !== undefined) {
      dispatch(fetchUsers())
        .then(unwrapResult)
        .catch(e => {
          console.error(e);
        });
    }
  }, [dispatch, usersStatus, me]);

  useEffect(() => {
    dispatch(fetchQuestions(props.examId))
      .then(unwrapResult)
      .then(() => {
        setQuestionsLoading(false);
      })
      .catch(e => {
        console.error(e);
      });
  }, [props.examId, dispatch]);

  const handleSubmit = (values: FormValues): Promise<any> => {
    const promises: Promise<any>[] = values.marks.map(mark => {
      if (mark.marks === undefined) {
        return Promise.resolve();
      }

      if (submissions[mark.studentId] === undefined) {
        return dispatch(createExamSubmission({
          examId: props.examId,
          studentId: mark.studentId,
          marks: mark.marks,
          answers: [],
        }))
          .then(unwrapResult)
          .catch(e => {
            console.error(e);
          });
      } else {
        return dispatch(updateExamSubmission({
          examId: props.examId,
          studentId: mark.studentId,
          marks: mark.marks,
          // eslint-disable-next-line @typescript-eslint/no-non-null-assertion
          eTag: submissions[mark.studentId]!.meta.eTag,
        }))
          .then(unwrapResult)
          .catch(e => {
            console.error(e);
          });
      }
    });

    return Promise.all(promises);
  };

  if (usersStatus !== "finished" || examSubmissionsLoading || questionsLoading) {
    return <Loader />;
  }

  return (
    <Formik
      initialValues={{ marks: students.map(s => ({
        studentId: s.id,
        // eslint-disable-next-line @typescript-eslint/no-non-null-assertion
        marks: submissions[s.id]?.marks === undefined ? undefined : (submissions[s.id]!.marks! < 0 ? undefined : submissions[s.id]!.marks),
      })) }}
      onSubmit={handleSubmit}
      validationSchema={FormSchema}
      enableReinitialize>
      {
        ({
          handleBlur,
          handleChange,
          handleSubmit,
          isSubmitting,
          touched,
          errors,
          values,
        }) => (
          <Form onSubmit={handleSubmit as any}>
            <Table striped bordered hover>
              <thead>
                <tr>
                  <th>Name</th>
                  <th>Marks</th>
                  <th>Details</th>
                </tr>
              </thead>
              <tbody>
                {
                  students.map((student, i) => (
                    <Fragment key={student.id}>
                      <tr>
                        <td>
                          {student.name}
                        </td>
                        <td>
                          <Form.Control
                            type="number"
                            name={`marks[${i}].marks`}
                            value={values.marks[i]?.marks}
                            onBlur={handleBlur}
                            onChange={handleChange}
                            min="0"
                            max={questions.reduce((a, b) => a + b.marks, 0)}
                            disabled={isSubmitting || me?.type === "STUDENT"}
                            isInvalid={
                              !!(
                                touched.marks
                                  && touched.marks[i]
                                  && errors.marks
                                  && errors.marks[i]
                              )
                            } />
                        </td>
                        <td>
                          <Button onClick={() => setSubmissionModalShow(student.id)}>
                            Show Details
                          </Button>
                        </td>
                      </tr>
                      <SubmissionModal
                        show={submissionModalShow === student.id}
                        onHide={() => setSubmissionModalShow(null)}
                        examId={props.examId}
                        studentId={student.id}
                        eTag={submissions[student.id]?.meta.eTag} />
                    </Fragment>
                  ))
                }
              </tbody>
            </Table>
            {
              me?.type !== "STUDENT" &&
                <Button type="submit" disabled={isSubmitting}>
                  Submit Marks
                </Button>
            }
          </Form>
        )
      }
    </Formik>
  );
};
