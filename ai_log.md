# AI Collaboration Log (`ai_log.md`)

This file documents how AI (ChatGPT / GPT-5.1 Thinking) was used as a collaborator for this project, including what prompts were used, what was adopted, and what was changed.

---

## 1. Database Design & SQL Scripts

### 1.1. Relational schema & DDL

**Rough prompt (paraphrased)**
> “Why can’t I drop Person? Here is my SQL. Help fix my foreign keys and table order.”   
> “Here is my full create/populate script. Why am I getting a foreign key error on Feedback?”

**What the AI suggested**

- Reordered `DROP TABLE` and `CREATE TABLE` statements to respect foreign key dependencies.
- Confirmed and adjusted FK constraints between:
    - `Person` → `Student` / `Counselor`
    - `Student` → `SelfAssessment`
    - `SelfAssessment` → `Referral`
    - `Appointment` → `Feedback`
- Pointed out that `Feedback` was referencing `AppointmentID` values that did not exist in the `Appointment` table, causing the FK error.
- Suggested using `ON DELETE CASCADE` on certain FKs if the intent was to automatically clean up related rows.

**What I adopted**

- The final drop order:

  ```sql
  DROP TABLE IF EXISTS Feedback;
  DROP TABLE IF EXISTS Appointment;
  DROP TABLE IF EXISTS Referral;
  DROP TABLE IF EXISTS SelfAssessment;
  DROP TABLE IF EXISTS Counselor;
  DROP TABLE IF EXISTS Student;
  DROP TABLE IF EXISTS Person;
