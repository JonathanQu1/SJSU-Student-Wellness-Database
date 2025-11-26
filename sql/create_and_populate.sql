//-----------Drop Tables if they exist---------------
DROP TABLE IF EXISTS Feedback;
DROP TABLE IF EXISTS Appointment;
DROP TABLE IF EXISTS Referral;
DROP TABLE IF EXISTS SelfAssessment;
DROP TABLE IF EXISTS Counselor;
DROP TABLE IF EXISTS Student;
DROP TABLE IF EXISTS Person;
DROP VIEW IF EXISTS vw_student_referral_summary;
DROP PROCEDURE IF EXISTS sp_add_selfassessment_once_per_day;

//-----------Create tables---------------
CREATE TABLE Person (
PersonID     INT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
Name         VARCHAR(100) NOT NULL,
ContactInfo  VARCHAR(255)
);
CREATE TABLE Student (
StudentID  INT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
PersonID   INT UNSIGNED NOT NULL UNIQUE,
Major      VARCHAR(100),
Year     VARCHAR(20),
CONSTRAINT fk_student_person
    FOREIGN KEY (PersonID) REFERENCES Person(PersonID)
ON UPDATE CASCADE ON DELETE CASCADE
);
CREATE TABLE Counselor (
CounselorID    INT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
PersonID       INT UNSIGNED NOT NULL UNIQUE,
Credentials    VARCHAR(255),
Specializations TEXT,
Availability    TEXT,
CONSTRAINT fk_counselor_person
    FOREIGN KEY (PersonID) REFERENCES Person(PersonID)
        ON UPDATE CASCADE ON DELETE CASCADE
);
CREATE TABLE SelfAssessment (
AssessmentID    INT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
StudentID       INT UNSIGNED NOT NULL,
Date          DATE NOT NULL,
AnxietyScore    INT UNSIGNED NOT NULL,
DepressionScore INT UNSIGNED NOT NULL,
StressScore     INT UNSIGNED NOT NULL,
CONSTRAINT chk_scores
    CHECK (AnxietyScore BETWEEN 0 AND 10
        AND DepressionScore BETWEEN 0 AND 10
        AND StressScore BETWEEN 0 AND 10),
    CONSTRAINT fk_assessment_student
        FOREIGN KEY (StudentID) REFERENCES Student(StudentID)
            ON UPDATE CASCADE ON DELETE CASCADE
);
CREATE TABLE Referral (
ReferralID   INT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
AssessmentID INT UNSIGNED NOT NULL,
CounselorID  INT UNSIGNED NOT NULL,
ReferralDate DATE NOT NULL,
Status       ENUM('Pending','Accepted','Declined','Closed') NOT NULL,
CONSTRAINT fk_referral_assessment
    FOREIGN KEY (AssessmentID) REFERENCES SelfAssessment(AssessmentID)
        ON UPDATE CASCADE ON DELETE CASCADE,
    CONSTRAINT fk_referral_counselor
    FOREIGN KEY (CounselorID) REFERENCES Counselor(CounselorID)
        ON UPDATE CASCADE ON DELETE CASCADE
);

CREATE TABLE Appointment (
AppointmentID INT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
StudentID     INT UNSIGNED NOT NULL,
CounselorID   INT UNSIGNED NOT NULL,
DateTime      DATETIME NOT NULL,
Status        ENUM('Scheduled','Completed','Cancelled','NoShow') NOT NULL,
`Mode`        ENUM('in-person','virtual') NOT NULL,
UNIQUE KEY uq_appt_student (StudentID, AppointmentID),
UNIQUE KEY uq_appt_counselor (CounselorID, AppointmentID),
CONSTRAINT fk_appt_student
    FOREIGN KEY (StudentID) REFERENCES Student(StudentID)
        ON UPDATE CASCADE ON DELETE CASCADE,
    CONSTRAINT fk_appt_counselor
    FOREIGN KEY (CounselorID) REFERENCES Counselor(CounselorID)
        ON UPDATE CASCADE ON DELETE CASCADE
);

CREATE TABLE Feedback (
AppointmentID INT UNSIGNED NOT NULL,
FeedbackSeq   INT UNSIGNED NOT NULL,
StudentID     INT UNSIGNED NOT NULL,
CounselorID   INT UNSIGNED NOT NULL,
SubmittedAt   TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
Rating        INT UNSIGNED NOT NULL,
Comments      TEXT,
PRIMARY KEY (AppointmentID, FeedbackSeq),
CONSTRAINT chk_rating CHECK (Rating BETWEEN 1 AND 5),
CONSTRAINT fk_feedback_appt
    FOREIGN KEY (AppointmentID) REFERENCES Appointment(AppointmentID)
        ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT fk_feedback_appt_student
    FOREIGN KEY (StudentID, AppointmentID)
        REFERENCES Appointment (StudentID, AppointmentID)
        ON UPDATE CASCADE,
    CONSTRAINT fk_feedback_appt_counselor
    FOREIGN KEY (CounselorID, AppointmentID)
        REFERENCES Appointment (CounselorID, AppointmentID)
            ON UPDATE CASCADE
);

//-----------Stored Routine---------------
DELIMITER //

DROP PROCEDURE IF EXISTS sp_add_selfassessment_once_per_day //
CREATE PROCEDURE sp_add_selfassessment_once_per_day (
    IN pStudentID     INT UNSIGNED,
    IN pDate          DATE,
    IN pAnxiety       INT,
    IN pDepression    INT,
    IN pStress        INT
)
BEGIN
    -- Rule: at most ONE self-assessment per student per day
    IF EXISTS (
        SELECT 1
        FROM SelfAssessment
        WHERE StudentID = pStudentID
          AND Date      = pDate
    ) THEN
        SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = 'Student already has a self-assessment for this date. No row inserted.';
ELSE
        INSERT INTO SelfAssessment (StudentID, Date, AnxietyScore, DepressionScore, StressScore)
        VALUES (pStudentID, pDate, pAnxiety, pDepression, pStress);
END IF;
END //

DELIMITER ;


//-----------Creates a view---------------
CREATE OR REPLACE VIEW vw_appointment_overview AS
SELECT
    a.AppointmentID,
    a.DateTime,
    a.Status,
    a.Mode,
    s.StudentID,
    sp.Name AS StudentName,
    c.CounselorID,
    cp.Name AS CounselorName
FROM Appointment a
         JOIN Student   s  ON s.StudentID  = a.StudentID
         JOIN Person    sp ON sp.PersonID  = s.PersonID
         JOIN Counselor c  ON c.CounselorID = a.CounselorID
         JOIN Person    cp ON cp.PersonID  = c.PersonID;

//-----------Inserts---------------
INSERT INTO Person (Name, ContactInfo) VALUES
('Avery Nguyen',   'avery.nguyen@sjsu.edu'),
('Jordan Patel',   'jordan.patel@sjsu.edu'),
('Maria Lopez',    'maria.lopez@sjsu.edu'),
('Ethan Chen',     'ethan.chen@sjsu.edu'),
('Priya Sharma',   'priya.sharma@sjsu.edu'),
('Noah Williams',  'noah.williams@sjsu.edu'),
('Sofia Rossi',    'sofia.rossi@sjsu.edu'),
('Liam Oconnor',   'liam.oconnor@sjsu.edu');
INSERT INTO Student (PersonID, Major, `Year`) VALUES
(1, 'CS',   'Senior'),
(2, 'SE',   'Junior'),
(3, 'PSY',  'Sophomore'),
(4, 'DS',   'Senior'),
(5, 'BUS',  'Junior'),
(6, 'MATH', 'Senior'),
(7, 'IS',   'Junior'),
(8, 'ME',   'Sophomore');
INSERT INTO Person (Name, ContactInfo) VALUES
('Dr. Alice Park',  'alice.park@sjsu.edu'),
('Dr. Brian Kim',   'brian.kim@sjsu.edu'),
('Dr. Carla Diaz',  'carla.diaz@sjsu.edu'),
('Dr. Omar Singh',  'omar.singh@sjsu.edu');

INSERT INTO Counselor (PersonID, Credentials, Specializations, Availability) VALUES
(9,  'LCSW', 'Anxiety; Depression',         'Mon-Fri 09:00-17:00'),
(10, 'LMFT', 'CBT; Couples',                 'Tue-Fri 10:00-18:00'),
(11, 'PhD',  'Trauma; PTSD',                 'Mon-Thu 08:00-16:00'),
(12, 'PsyD', 'Adolescent; Stress Management','Wed-Sat 11:00-19:00');

INSERT INTO SelfAssessment (StudentID, `Date`, AnxietyScore, DepressionScore, StressScore) VALUES
(1, '2025-11-01', 5, 4, 6),
(1, '2025-11-15', 4, 4, 6),   -- same student, multiple self-assessments
(2, '2025-11-03', 3, 3, 3),
(3, '2025-11-04', 7, 7, 7),
(4, '2025-11-02', 2, 3, 2),
(5, '2025-11-01', 6, 5, 5),
(6, '2025-11-06', 3, 3, 3),
(7, '2025-11-07', 8, 6, 8),
(8, '2025-11-08', 4, 4, 4);

INSERT INTO Referral (AssessmentID, CounselorID, ReferralDate, Status) VALUES
(1, 1, '2025-11-01', 'Pending'),
(2, 1, '2025-11-15', 'Accepted'),
(3, 2, '2025-11-03', 'Pending'),
(4, 3, '2025-11-04', 'Closed'),
(5, 4, '2025-11-02', 'Declined'),
(6, 1, '2025-11-01', 'Accepted'),
(7, 1, '2025-11-06', 'Pending'),
(8, 2, '2025-11-07', 'Pending'),
(9, 4, '2025-11-08', 'Accepted');

INSERT INTO Appointment (StudentID, CounselorID, `DateTime`, Status, `Mode`) VALUES
(1, 1, '2025-11-05 10:00:00', 'Scheduled', 'virtual'),
(2, 2, '2025-11-06 14:30:00', 'Completed', 'in-person'),
(3, 3, '2025-11-07 09:00:00',  'Scheduled', 'in-person'),
(4, 4, '2025-11-08 11:15:00', 'Cancelled', 'virtual'),
(5, 1, '2025-11-09 13:45:00', 'NoShow',   'in-person'),
(6, 2, '2025-11-10 16:00:00', 'Completed','virtual'),
(7, 3, '2025-11-11 15:30:00', 'Scheduled','in-person'),
(8, 4, '2025-11-12 10:45:00', 'Scheduled','virtual');

INSERT INTO Feedback (AppointmentID, FeedbackSeq, StudentID, CounselorID, SubmittedAt, Rating, Comments) VALUES
(1, 1, 1, 1, '2025-11-05 11:05:00', 5, 'Very helpful.'),
(1, 2, 1, 1, '2025-11-05 20:15:00', 4, 'Follow-up thoughts.'),
(2, 1, 2, 2, '2025-11-06 17:00:00', 5, 'Felt understood.'),
(3, 1, 3, 3, '2025-11-07 10:30:00', 4, 'Clear plan.'),
(6, 1, 6, 2, '2025-11-10 17:10:00', 5, 'Great session.'),
(7, 1, 7, 3, '2025-11-11 16:00:00', 4, 'Good strategies.'),
(7, 2, 7, 3, '2025-11-12 09:00:00', 5, 'Extra feedback after trying tips.'),
(8, 1, 8, 4, '2025-11-12 12:00:00', 4, 'Helpful.');
