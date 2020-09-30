import React, { useEffect, useState } from "react";
import { Button, Card } from "react-bootstrap";
import { useSelector } from "react-redux";
import { ExamState, selectExamById } from "../../redux/slices/examsSlice";
import { deleteQuestion, fetchQuestions, questionLabels, selectQuestionsByIds } from "../../redux/slices/questionsSlice";
import { selectMe } from "../../redux/slices/usersSlice";
import { RootState, useAppDispatch } from "../../redux/store";
import { QuestionModal } from "./questionModal";
import styles from "./questions.module.scss";

interface QuestionProps {
  examId: string,
}

export const Questions = (props: QuestionProps): JSX.Element => {
  const dispatch = useAppDispatch();
  const exam = useSelector<RootState, ExamState | undefined>(
    state => selectExamById(state, props.examId)
  );
  const questions = useSelector(selectQuestionsByIds(exam?.questionIds ?? []));
  const [questionModalShow, setQuestionModalShow] = useState<string | null>(null);
  const me = useSelector(selectMe);

  useEffect(() => {
    dispatch(fetchQuestions(props.examId));
  }, [props.examId, dispatch]);

  const onClick = (id: string): void => {
    dispatch(deleteQuestion({
      questionId: id,
    }))
      .catch(e => {
        console.error(e);
      });
  };

  return (
    <div className={styles.wrapper}>
      {
        questions.map(question => {
          return (
            <Card key={question.id}>
              <Card.Header>{question.question}</Card.Header>
              <Card.Body>
                <Card.Text>
                  Marks: {question.marks}
                  <br />
                  Type: {questionLabels[question.type]}
                </Card.Text>
                {
                  me?.type === "INSTRUCTOR" &&
                    <>
                      <Button className="mr-2" onClick={() => setQuestionModalShow(question.id)}>
                        Edit
                      </Button>
                      <Button className="mr-2" onClick={() => onClick(question.id)}>
                        Delete
                      </Button>
                    </>
                }
                <QuestionModal
                  show={questionModalShow === question.id}
                  onHide={() => setQuestionModalShow(null)}
                  id={question.id}
                  question={question.question}
                  marks={question.marks}
                  type={question.type}
                  optionIds={question.optionIds} />
              </Card.Body>
            </Card>
          );
        })
      }
    </div>
  );
};
