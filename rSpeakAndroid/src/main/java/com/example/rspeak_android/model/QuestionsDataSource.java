package com.example.rspeak_android.model;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;
import java.util.List;

public class QuestionsDataSource {
    // Database Fields
    private SQLiteDatabase database;
    private RSpeakSQLiteHelper dbHelper;
    private String[] allColumns = {
            RSpeakSQLiteHelper.QUESTIONS_COLUMN_ID,
            RSpeakSQLiteHelper.QUESTIONS_COLUMN_CONTENT,
            RSpeakSQLiteHelper.QUESTIONS_COLUMN_TIME_POSTED,
            RSpeakSQLiteHelper.QUESTIONS_COLUMN_ON_ASKER_DEVICE };
    private ThreadsDataSource threadsDataSource;

    private static int ON_ASKER_DEVICE_TRUE = 1;
    private static int ON_ASKER_DEVICE_FALSE = 0;

    public QuestionsDataSource(Context context) {
        dbHelper = new RSpeakSQLiteHelper(context);

        threadsDataSource = new ThreadsDataSource(context);
        threadsDataSource.open();
    }

    public void open() throws SQLException {
        database = dbHelper.getWritableDatabase();
    }

    public void close() {
        dbHelper.close();
        threadsDataSource.close();
    }

    public Question createQuestion(String question_content,
                                   long time_posted,
                                   boolean currently_on_asker_device) {
        ContentValues values = new ContentValues();

        values.put(RSpeakSQLiteHelper.QUESTIONS_COLUMN_CONTENT, question_content);
        values.put(RSpeakSQLiteHelper.QUESTIONS_COLUMN_TIME_POSTED, time_posted);
        values.put(RSpeakSQLiteHelper.QUESTIONS_COLUMN_ON_ASKER_DEVICE, currently_on_asker_device);


        long question_id = database.insert(RSpeakSQLiteHelper.TABLE_QUESTIONS, null, values);
        Cursor cursor = database.query(RSpeakSQLiteHelper.TABLE_QUESTIONS,
                allColumns,
                RSpeakSQLiteHelper.QUESTIONS_COLUMN_ID + " = " + question_id,
                null,
                null,
                null,
                null);
        cursor.moveToFirst();
        Question newQuestion = cursorToQuestion(cursor);
        cursor.close();
        return newQuestion;
    }

    public void deleteQuestion(Question question) {
        long question_id = question.getID();

        database.delete(RSpeakSQLiteHelper.TABLE_QUESTIONS,
                RSpeakSQLiteHelper.QUESTIONS_COLUMN_ID + " = " + question_id,
                null);
    }

    private List<Question> queryAllQuestions(String conditions) {
        List<Question> questions = new ArrayList<Question>();

        Cursor cursor = database.query(RSpeakSQLiteHelper.TABLE_QUESTIONS,
                allColumns,
                conditions,
                null,
                null,
                null,
                RSpeakSQLiteHelper.QUESTIONS_COLUMN_TIME_POSTED + " DESC");

        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            Question question = cursorToQuestion(cursor);
            questions.add(question);
            cursor.moveToNext();
        }

        cursor.close();
        return questions;
    }

    public List<Question> getAllQuestions() {
        return queryAllQuestions(null);
    }

    public Question getQuestionById(long question_id) {
        List<Question> questions = queryAllQuestions(
                RSpeakSQLiteHelper.QUESTIONS_COLUMN_ID +
                        " = '" +
                        question_id +
                        "'");

        return (questions.size() == 0 ? null : questions.get(0));
    }

    public List<Question> getLocallyAskedQuestions() {
        return queryAllQuestions(RSpeakSQLiteHelper.QUESTIONS_COLUMN_ON_ASKER_DEVICE + " = " + ON_ASKER_DEVICE_TRUE);
    }

    public List<Question> getForeignAskedQuestions() {
        return queryAllQuestions(RSpeakSQLiteHelper.QUESTIONS_COLUMN_ON_ASKER_DEVICE + " = " + ON_ASKER_DEVICE_FALSE);
    }

    private Question cursorToQuestion(Cursor cursor) {
        Question question = new Question();

        question.setID(
                cursor.getLong(
                        cursor.getColumnIndex(RSpeakSQLiteHelper.QUESTIONS_COLUMN_ID)));
        question.setQuestionContent(
                cursor.getString(
                        cursor.getColumnIndex(RSpeakSQLiteHelper.QUESTIONS_COLUMN_CONTENT)));
        question.setTimePosted(
                cursor.getLong(
                        cursor.getColumnIndex(RSpeakSQLiteHelper.QUESTIONS_COLUMN_TIME_POSTED)));
        question.setCurrentlyOnAskerDevice(
                cursor.getInt(
                        cursor.getColumnIndex(RSpeakSQLiteHelper.QUESTIONS_COLUMN_ON_ASKER_DEVICE)) == ON_ASKER_DEVICE_TRUE);
        question.setThreads(
                threadsDataSource.getThreadsByQuestionID(question.getID()));

        return question;
    }
}
