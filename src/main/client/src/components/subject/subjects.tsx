import React, { useEffect } from "react";
import { Container, Card } from "react-bootstrap";
import { useSelector } from "react-redux";
import { Link } from "react-router-dom";
import { fetchSubjects, selectAllSubjects, selectSubjectsStatus } from "../../redux/slices/subjectsSlice";
import { useAppDispatch } from "../../redux/store";
import styles from "./subjects.module.scss";

export const Subjects = (): JSX.Element => {
  const dispatch = useAppDispatch();
  const subjectsStatus = useSelector(selectSubjectsStatus);
  const subjects = useSelector(selectAllSubjects);

  useEffect(() => {
    if (subjectsStatus === "idle") {
      dispatch(fetchSubjects());
    }
  }, [dispatch, subjectsStatus]);

  return (
    <Container className={styles.wrapper}>
      {
        subjects.map(subject => {
          return (
            <Card key={subject.id}>
              <Card.Body>
                <Card.Title>
                  <Link to={`/subjects/${subject.slug}`}>
                    {subject.name}
                  </Link>
                </Card.Title>
                <Card.Text>{subject.description}</Card.Text>
              </Card.Body>
            </Card>
          );
        })
      }
    </Container>
  );
};