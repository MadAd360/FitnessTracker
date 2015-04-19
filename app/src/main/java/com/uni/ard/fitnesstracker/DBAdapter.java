package com.uni.ard.fitnesstracker;

import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.preference.PreferenceManager;
import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.text.DateFormat;
import java.util.Date;

/**
 * Simple notes database access helper class. Defines the basic CRUD operations
 * for the notepad example, and gives the ability to list all notes as well as
 * retrieve or modify a specific note.
 * <p/>
 * This has been improved from the first version of this tutorial through the
 * addition of better error handling and also using returning a Cursor instead
 * of using a collection of inner classes (which is less scalable and not
 * recommended).
 */
public class DBAdapter {

    public static final String KEY_GOAL_TITLE = "title";
    public static final String KEY_GOAL_WALK = "walktotal";
    public static final String KEY_GOAL_CLIMB = "climbtotal";
    public static final String KEY_GOAL_START = "start";
    public static final String KEY_GOAL_END = "end";
    public static final String KEY_GOAL_COMPLETE = "complete";
    public static final String KEY_GOAL_ACTIVE = "active";
    public static final String KEY_GOAL_PREVIOUS_STATE = "previousstate";
    public static final String KEY_GOAL_DUAL = "dualType";
    public static final String KEY_GOAL_WALK_UNIT = "walkunit";
    public static final String KEY_GOAL_CLIMB_UNIT = "climbunit";
    public static final String KEY_GOAL_FACEBOOK_ID = "facebookid";
    public static final String KEY_GOAL_TWITTER_ID = "twitterid";
    public static final String KEY_GOAL_MAP = "map";
    public static final String KEY_GOAL_CALORIE = "calorie";
    public static final String KEY_GOAL_CHALLENGE = "challenge";

    public static final String KEY_ACTIVITY_NUMBER = "number";
    public static final String KEY_ACTIVITY_DATE = "date";
    public static final String KEY_ACTIVITY_UNIT = "unit";
    public static final String KEY_TYPE = "climb";
    public static final String KEY_ROWID = "_id";
    public static final String KEY_ACTIVITY_GOAL = "goalAttached";

    public static final String KEY_HISTORY_DATE = "date";
    public static final String KEY_HISTORY_MESSAGE = "message";
    public static final String KEY_HISTORY_GOAL = "goalAttached";

    public static final String KEY_MAP_START_LAT = "startlat";
    public static final String KEY_MAP_START_LONG = "startlong";
    public static final String KEY_MAP_END_LAT = "endlat";
    public static final String KEY_MAP_END_LONG = "endlong";

    public static final String KEY_TREAT_NAME = "name";
    public static final String KEY_TREAT_CALORIES = "calories";
    public static final String KEY_TREAT_IMAGE = "image";

    public static final String KEY_CHALLENGE_OPPONENT = "opponent";
    public static final String KEY_CHALLENGE_PENALTY = "penalty";
    public static final String KEY_CHALLENGE_LOST = "lost";
    public static final String KEY_CHALLENGE_WON = "won";
    public static final String KEY_CHALLENGE_FINISH = "finish";


    private static final String TAG = "DBAdapter";
    private DatabaseHelper mDbHelper;
    private SQLiteDatabase mDb;


    private static final String DATABASE_NAME = "data";
    private static final String GOAL_TABLE = "goals";
    private static final String MAP_TABLE = "maps";
    private static final String ACTIVITY_TABLE = "activities";
    private static final String TREAT_TABLE = "treats";
    private static final String HISTORY_TABLE = "history";
    private static final String CHALLENGE_TABLE = "challenges";
    private static final int DATABASE_VERSION = 2;

    /**
     * Database creation sql statement
     */
    private static final String GOAL_CREATE =
            "create table " + GOAL_TABLE + " (_id integer primary key autoincrement, "
                    + "title text not null, walktotal integer not null, climbtotal integer not null, " +
                    "walkunit text not null, climbunit text not null, " +
                    "start integer not null, end integer not null, complete boolean not null, climb boolean not null, " +
                    "dualType boolean not null, active boolean not null, previousstate int not null, " +
                    "facebookid text, twitterid int, map integer, calorie integer, challenge integer, " +
                    "FOREIGN KEY(map) REFERENCES maps(_id),  FOREIGN KEY(calorie) REFERENCES treats(_id)," +
                    "FOREIGN KEY(challenge) REFERENCES challenges(_id));";

    private static final String ACTIVITY_CREATE =
            "create table " + ACTIVITY_TABLE + " (_id integer primary key autoincrement, "
                    + "number integer not null, date integer not null, unit text not null, " +
                    "climb boolean not null, goalAttached integer, " +
                    "FOREIGN KEY(goalAttached) REFERENCES goals(_id));";

    private static final String HISTORY_CREATE =
            "create table " + HISTORY_TABLE + " (_id integer primary key autoincrement, "
                    + "date integer not null, message text not null, goalAttached integer, " +
                    "FOREIGN KEY(goalAttached) REFERENCES goals(_id));";

    private static final String MAP_CREATE =
            "create table " + MAP_TABLE + " (_id integer primary key autoincrement, "
                    + "startlat real not null, startlong real not null, " +
                    "endlat real not null, endlong real not null);";


    private static final String TREAT_CREATE =
            "create table " + TREAT_TABLE + " (_id integer primary key autoincrement, "
                    + "name text not null, calories integer not null, " +
                    "image blob not null);";

    private static final String CHALLENGE_CREATE =
            "create table " + CHALLENGE_TABLE + " (_id integer primary key autoincrement, "
                    + "opponent text, penalty integer, lost boolean, won boolean, finish integer);";

    private final Context mCtx;

    private static class DatabaseHelper extends SQLiteOpenHelper {

        DatabaseHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {

            db.execSQL("PRAGMA foreign_keys = ON;");

            db.execSQL(GOAL_CREATE);
            db.execSQL(ACTIVITY_CREATE);
            db.execSQL(HISTORY_CREATE);
            db.execSQL(MAP_CREATE);
            db.execSQL(TREAT_CREATE);
            db.execSQL(CHALLENGE_CREATE);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            Log.w(TAG, "Upgrading database from version " + oldVersion + " to "
                    + newVersion + ", which will destroy all old data");
            db.execSQL("DROP TABLE IF EXISTS notes");
            onCreate(db);
        }
    }

    /**
     * Constructor - takes the context to allow the database to be
     * opened/created
     *
     * @param ctx the Context within which to work
     */
    public DBAdapter(Context ctx) {
        this.mCtx = ctx;
    }

    /**
     * Open the notes database. If it cannot be opened, try to create a new
     * instance of the database. If it cannot be created, throw an exception to
     * signal the failure
     *
     * @return this (self reference, allowing this to be chained in an
     * initialization call)
     * @throws SQLException if the database could be neither opened or created
     */
    public DBAdapter open() throws SQLException {
        mDbHelper = new DatabaseHelper(mCtx);
        mDb = mDbHelper.getWritableDatabase();
        if(!fetchTreats().isFirst()){
            Bitmap bm = BitmapFactory.decodeResource(mCtx.getResources(), R.drawable.ice_cream);
            insertTreat("Ice Cream", bm, 100);
        }
        return this;
    }

    public void close() {
        mDbHelper.close();
    }


    public long createGoal(String title, Integer walkGoal, Integer climbGoal, String walkUnit, String climbUnit, Long start, Long end,
                           Boolean type, boolean both, Boolean complete, Boolean active, int startStatus) {
        ContentValues initialValues = new ContentValues();
        initialValues.put(KEY_GOAL_TITLE, title);
        initialValues.put(KEY_GOAL_WALK, walkGoal);
        initialValues.put(KEY_GOAL_CLIMB, climbGoal);
        initialValues.put(KEY_GOAL_WALK_UNIT, walkUnit);
        initialValues.put(KEY_GOAL_CLIMB_UNIT, climbUnit);
        initialValues.put(KEY_GOAL_START, start);
        initialValues.put(KEY_GOAL_END, end);
        initialValues.put(KEY_TYPE, type);
        initialValues.put(KEY_GOAL_DUAL, both);
        initialValues.put(KEY_GOAL_COMPLETE, complete);
        initialValues.put(KEY_GOAL_ACTIVE, active);
        initialValues.put(KEY_GOAL_PREVIOUS_STATE, startStatus);


        Long rowID = mDb.insert(GOAL_TABLE, null, initialValues);

        addHistory("Goal created", GlobalVariables.getTime(), rowID);

        return rowID;
    }

    public long createActivity(Integer value, String unit, Boolean type, Long goalNumber, Long date) {
        ContentValues initialValues = new ContentValues();
        initialValues.put(KEY_ACTIVITY_NUMBER, value);
        initialValues.put(KEY_TYPE, type);
        initialValues.put(KEY_ACTIVITY_GOAL, goalNumber);
        initialValues.put(KEY_ACTIVITY_DATE, date);
        initialValues.put(KEY_ACTIVITY_UNIT, unit);


        Long rowID = mDb.insert(ACTIVITY_TABLE, null, initialValues);

        addHistory("Progress increased by " + value + " " + unit, date, goalNumber);

        Cursor goalCursor = fetchGoal(goalNumber);
        Double walkProgress = getGoalWalkProgress(goalNumber);
        Double climbProgress = getGoalClimbProgress(goalNumber);
        if (goalCursor.moveToFirst()) {

            String goalName = goalCursor.getString(goalCursor.getColumnIndexOrThrow(KEY_GOAL_TITLE));
            String goalClimbUnit = goalCursor.getString(goalCursor.getColumnIndexOrThrow(KEY_GOAL_CLIMB_UNIT));
            String goalWalkUnit = goalCursor.getString(goalCursor.getColumnIndexOrThrow(KEY_GOAL_WALK_UNIT));

            Long calorieId = goalCursor.getLong(goalCursor.getColumnIndexOrThrow(KEY_GOAL_CALORIE));

            Long challengeId = goalCursor.getLong(goalCursor.getColumnIndexOrThrow(KEY_GOAL_CHALLENGE));
            Long endDate = goalCursor.getLong(goalCursor.getColumnIndexOrThrow(KEY_GOAL_END));


            Integer walkTotal = goalCursor.getInt(goalCursor.getColumnIndexOrThrow(KEY_GOAL_WALK));
            Integer climbTotal = goalCursor.getInt(goalCursor.getColumnIndexOrThrow(KEY_GOAL_CLIMB));
            if (walkProgress == null) {
                walkProgress = 0.0;
            }

            if (climbProgress == null) {
                climbProgress = 0.0;
            }

            if(calorieId != null){
                walkProgress = walkProgress + climbProgress;
                climbProgress = 0.0;
                climbTotal = 0;
            }

            if (walkProgress >= walkTotal && climbProgress >= climbTotal) {
                GlobalVariables.createNotification(goalNumber, goalName, GlobalVariables.GOAL_MODE_COMPLETED, mCtx);
                updateGoalComplete(goalNumber, true);
                SharedPreferences sp = PreferenceManager
                        .getDefaultSharedPreferences(mCtx);
                int freeCalories = sp.getInt("pref_calorie_free", 0);
                int calorieClimbTotal = (int)GlobalVariables.convertUnits(climbTotal, true, "Calories", goalClimbUnit);
                int calorieWalkTotal = (int)GlobalVariables.convertUnits(walkTotal, false, "Calories", goalWalkUnit);
                sp.edit().putInt("pref_calorie_free", freeCalories + calorieClimbTotal + calorieWalkTotal).apply();

                if(challengeId != 0){
                    updateChallengeEnd(challengeId, endDate);
                }
            }
        }
        return rowID;
    }

    public long updateActivity(Long rowID, Integer value, String unit, Boolean type, Long goalNumber, Long date) {
        ContentValues args = new ContentValues();
        args.put(KEY_ACTIVITY_NUMBER, value);
        args.put(KEY_TYPE, type);
        args.put(KEY_ACTIVITY_GOAL, goalNumber);
        args.put(KEY_ACTIVITY_DATE, date);
        args.put(KEY_ACTIVITY_UNIT, unit);

        mDb.update(ACTIVITY_TABLE, args, KEY_ROWID + "=" + rowID, null);

        addHistory("Progress increased by " + value + " " + unit, date, goalNumber);

        Cursor goalCursor = fetchGoal(goalNumber);
        Double walkProgress = getGoalWalkProgress(goalNumber);
        Double climbProgress = getGoalClimbProgress(goalNumber);
        if (goalCursor.moveToFirst()) {

            String goalName = goalCursor.getString(goalCursor.getColumnIndexOrThrow(KEY_GOAL_TITLE));

            Integer walkTotal = goalCursor.getInt(goalCursor.getColumnIndexOrThrow(KEY_GOAL_WALK));
            Integer climbTotal = goalCursor.getInt(goalCursor.getColumnIndexOrThrow(KEY_GOAL_CLIMB));
            if (walkProgress == null) {
                walkProgress = 0.0;
            }

            if (climbProgress == null) {
                climbProgress = 0.0;
            }

            if (walkProgress >= walkTotal && climbProgress >= climbTotal) {
                GlobalVariables.createNotification(goalNumber, goalName, GlobalVariables.GOAL_MODE_COMPLETED, mCtx);
                updateGoalComplete(goalNumber, true);
            }
        }
        return rowID;
    }

    public int updateGoalComplete(Long rowID, boolean updateValue) {
        ContentValues args = new ContentValues();
        args.put(KEY_GOAL_COMPLETE, updateValue);

        return mDb.update(GOAL_TABLE, args, KEY_ROWID + "=" + rowID, null);
    }

    public boolean deleteActivity(long rowId) {

        return mDb.delete(ACTIVITY_TABLE, KEY_ROWID + "=" + rowId, null) > 0;
    }

    /**
     * Return a Cursor over the list of all notes in the database
     *
     * @return Cursor over all notes
     */
    public Cursor fetchAllGoals() {


        String order = KEY_GOAL_COMPLETE + " ASC, " + KEY_GOAL_ACTIVE + " DESC, " + KEY_GOAL_START + " ASC, " +
                KEY_GOAL_END + " ASC ";

        return mDb.query(GOAL_TABLE, new String[]{KEY_ROWID, KEY_GOAL_TITLE, KEY_GOAL_WALK, KEY_GOAL_CLIMB, KEY_GOAL_DUAL,
                KEY_GOAL_WALK_UNIT, KEY_GOAL_CLIMB_UNIT, KEY_GOAL_START, KEY_GOAL_END, KEY_GOAL_COMPLETE, KEY_TYPE,
                KEY_GOAL_ACTIVE, KEY_GOAL_CALORIE, KEY_GOAL_CHALLENGE}, null, null, null, null, order);
    }


    public Cursor fetchLogGoals(boolean climbType) {

        Long current = GlobalVariables.getTime();

        int climb = 0;

        if (climbType) {
            climb = 1;
        }

        String where = DBAdapter.KEY_GOAL_ACTIVE + "=" + 1 + " AND " +
                "(" + DBAdapter.KEY_TYPE + "=" + climb + " OR " + DBAdapter.KEY_GOAL_DUAL + "=" + 1 +
                " OR " + DBAdapter.KEY_GOAL_CALORIE + " NOT NULL" + ")" +
                " AND " + DBAdapter.KEY_GOAL_COMPLETE + "=" + 0 + " AND " + DBAdapter.KEY_GOAL_START + "<" + current;

        String order = KEY_GOAL_END + " ASC ";

        return mDb.query(GOAL_TABLE, new String[]{KEY_ROWID, KEY_GOAL_TITLE, KEY_GOAL_WALK, KEY_GOAL_CLIMB,
                KEY_GOAL_WALK_UNIT, KEY_GOAL_CLIMB_UNIT, KEY_GOAL_START, KEY_GOAL_END, KEY_GOAL_COMPLETE, KEY_TYPE, KEY_GOAL_ACTIVE}, where, null, null, null, order);
    }

    public Cursor fetchActiveGoals() {

        Long current = GlobalVariables.getTime();

        String where = KEY_GOAL_ACTIVE + "=" + 1 + " AND " + DBAdapter.KEY_GOAL_COMPLETE + "=" + 0 + " AND " + DBAdapter.KEY_GOAL_START + "<=" + current;

        String order = KEY_GOAL_END + " ASC ";

        return mDb.query(GOAL_TABLE, new String[]{KEY_ROWID, KEY_GOAL_TITLE, KEY_GOAL_WALK, KEY_GOAL_CLIMB, KEY_GOAL_DUAL,
                KEY_GOAL_WALK_UNIT, KEY_GOAL_CLIMB_UNIT, KEY_GOAL_START, KEY_GOAL_END, KEY_GOAL_COMPLETE,
                KEY_TYPE, KEY_GOAL_ACTIVE, KEY_GOAL_CALORIE, KEY_GOAL_CHALLENGE}, where, null, null, null, order);
    }

    public Cursor fetchAllActivities() {


        String order = KEY_ACTIVITY_DATE + " DESC ";

        return mDb.query(ACTIVITY_TABLE, new String[]{KEY_ROWID, KEY_ACTIVITY_NUMBER,
                KEY_ACTIVITY_UNIT, KEY_TYPE, KEY_ACTIVITY_DATE}, null, null, null, null, order);
    }

    public Cursor fetchActivity(Long rowId) {

        Cursor mCursor =

                mDb.query(ACTIVITY_TABLE, new String[]{KEY_ROWID, KEY_ACTIVITY_NUMBER,
                        KEY_ACTIVITY_UNIT, KEY_TYPE, KEY_ACTIVITY_DATE, KEY_ACTIVITY_GOAL}, KEY_ROWID + "=" + rowId, null, null, null, null);
        if (mCursor != null) {
            mCursor.moveToFirst();
        }
        return mCursor;
    }


    /**
     * Return a Cursor positioned at the note that matches the given rowId
     *
     * @param rowId id of note to retrieve
     * @return Cursor positioned to matching note, if found
     * @throws SQLException if note could not be found/retrieved
     */
    public Cursor fetchGoal(long rowId) throws SQLException {

        Cursor mCursor =

                mDb.query(true, GOAL_TABLE, new String[]{KEY_ROWID, KEY_GOAL_WALK, KEY_GOAL_CLIMB, KEY_GOAL_TITLE, KEY_GOAL_PREVIOUS_STATE,
                                KEY_GOAL_WALK_UNIT, KEY_GOAL_CLIMB_UNIT, KEY_GOAL_START, KEY_GOAL_END, KEY_GOAL_COMPLETE, KEY_TYPE,
                                KEY_GOAL_DUAL, KEY_GOAL_ACTIVE, KEY_GOAL_FACEBOOK_ID, KEY_GOAL_TWITTER_ID, KEY_GOAL_MAP, KEY_GOAL_CALORIE, KEY_GOAL_CHALLENGE}, KEY_ROWID + "=" + rowId, null,
                        null, null, null, null);
        if (mCursor != null) {
            mCursor.moveToFirst();
        }
        return mCursor;

    }


    public boolean updateGoal(long rowId, String title, Integer walkGoal, Integer climbGoal, String walkUnit, String climbUnit, Long start, Long end, Boolean type, Boolean both) {
        ContentValues args = new ContentValues();
        args.put(KEY_GOAL_TITLE, title);

        Cursor goalCursor = fetchGoal(rowId);
        if (goalCursor.moveToFirst()) {
            String oldTitle = goalCursor.getString(goalCursor.getColumnIndexOrThrow(DBAdapter.KEY_GOAL_TITLE));
            int oldWalkGoal = goalCursor.getInt(goalCursor.getColumnIndexOrThrow(DBAdapter.KEY_GOAL_WALK));
            int oldClimbGoal = goalCursor.getInt(goalCursor.getColumnIndexOrThrow(DBAdapter.KEY_GOAL_CLIMB));
            Long oldStartNumber = goalCursor.getLong(goalCursor.getColumnIndexOrThrow(DBAdapter.KEY_GOAL_START));
            Long oldEndNumber = goalCursor.getLong(goalCursor.getColumnIndexOrThrow(DBAdapter.KEY_GOAL_END));
            String oldWalkUnit = goalCursor.getString(goalCursor.getColumnIndexOrThrow(DBAdapter.KEY_GOAL_WALK_UNIT));
            String oldClimbUnit = goalCursor.getString(goalCursor.getColumnIndexOrThrow(DBAdapter.KEY_GOAL_CLIMB_UNIT));
            boolean oldType = goalCursor.getInt(
                    goalCursor.getColumnIndexOrThrow(DBAdapter.KEY_TYPE)) > 0;
            boolean oldBoth = goalCursor.getInt(
                    goalCursor.getColumnIndexOrThrow(DBAdapter.KEY_GOAL_DUAL)) > 0;


            DateFormat dateFormat = android.text.format.DateFormat.getDateFormat(mCtx);


            if (!oldTitle.equals(title)) {
                addHistory("Changed name from " + oldTitle + " to " + title, GlobalVariables.getTime(), rowId);
            }
            if (oldWalkGoal != walkGoal) {
                addHistory("Changed walking target from " + oldWalkGoal + " to " + walkGoal, GlobalVariables.getTime(), rowId);
                args.put(KEY_GOAL_WALK, walkGoal);
            }
            if (oldClimbGoal != climbGoal) {
                addHistory("Changed climbing target from " + oldClimbGoal + " to " + climbGoal, GlobalVariables.getTime(), rowId);
                args.put(KEY_GOAL_CLIMB, climbGoal);
            }
            if (!oldWalkUnit.equals(walkUnit)) {
                addHistory("Changed unit from " + oldWalkUnit + " to " + walkUnit, GlobalVariables.getTime(), rowId);
                args.put(KEY_GOAL_WALK_UNIT, walkUnit);
            }
            if (!oldClimbUnit.equals(climbUnit)) {
                addHistory("Changed unit from " + oldClimbUnit + " to " + climbUnit, GlobalVariables.getTime(), rowId);
                args.put(KEY_GOAL_CLIMB_UNIT, climbUnit);
            }
            if (oldStartNumber.compareTo(start) != 0) {
                String oldDate = dateFormat.format(new Date(oldStartNumber));
                String newDate = dateFormat.format(new Date(start));
                addHistory("Changed start date from " + oldDate + " to " + newDate, GlobalVariables.getTime(), rowId);
                args.put(KEY_GOAL_START, start);
            }
            if (oldEndNumber.compareTo(end) != 0) {
                String oldDate = dateFormat.format(new Date(oldEndNumber));
                String newDate = dateFormat.format(new Date(end));
                addHistory("Changed end date from " + oldDate + " to " + newDate, GlobalVariables.getTime(), rowId);
                args.put(KEY_GOAL_END, end);
            }
            if (oldBoth != both) {
                String typeText = "goalBoth types";
                String oldTypeText = "climbed";
                if (!oldType) {
                    oldTypeText = "walked";
                }
                addHistory("Changed goalType from " + oldTypeText + " to " + typeText, GlobalVariables.getTime(), rowId);
                args.put(KEY_GOAL_DUAL, both);
            } else if (oldType != type) {
                String typeText = "walked";
                String oldTypeText = "climbed";
                if (type) {
                    typeText = "climbed";
                    oldTypeText = "walked";
                }
                addHistory("Changed goalType from " + oldTypeText + " to " + typeText, GlobalVariables.getTime(), rowId);
                args.put(KEY_TYPE, type);
            }
        }

        return mDb.update(GOAL_TABLE, args, KEY_ROWID + "=" + rowId, null) > 0;
    }

    public boolean updateGoalPreviousState(long rowId, int newState) {
        ContentValues args = new ContentValues();
        args.put(KEY_GOAL_PREVIOUS_STATE, newState);

        Cursor goalCursor = fetchGoal(rowId);
        if (goalCursor.moveToFirst()) {
            int previousState = goalCursor.getInt(goalCursor.getColumnIndexOrThrow(DBAdapter.KEY_GOAL_PREVIOUS_STATE));
            String previousText = getStatusString(previousState);
            String newText = getStatusString(newState);
            if (previousState != newState) {
                addHistory("Changed status from " + previousText + " to " + newText, GlobalVariables.getTime(), rowId);
                if (newState == GlobalVariables.GOAL_MODE_OVERDUE) {
                    String goalName = goalCursor.getString(goalCursor.getColumnIndexOrThrow(DBAdapter.KEY_GOAL_TITLE));
                    GlobalVariables.createNotification(rowId, goalName, newState, mCtx);
                }
            }
        }
        return mDb.update(GOAL_TABLE, args, KEY_ROWID + "=" + rowId, null) > 0;
    }

    public boolean updateGoalActive(long rowId, Boolean active) {
        ContentValues args = new ContentValues();
        args.put(KEY_GOAL_ACTIVE, active);

        return mDb.update(GOAL_TABLE, args, KEY_ROWID + "=" + rowId, null) > 0;
    }


//    public boolean updateActivity(long rowId, Integer number, String unit, String goalType) {
//        ContentValues initialValues = new ContentValues();
//        initialValues.put(KEY_ACTIVITY_NUMBER, number);
//        initialValues.put(KEY_UNIT, unit);
//        initialValues.put(KEY_TYPE, goalType);
//
//        return mDb.update(ACTIVITY_TABLE, initialValues, KEY_ROWID + "=" + rowId, null) > 0;
//    }


    public Long addHistory(String message, Long date, Long goalNumber) {
        ContentValues initialValues = new ContentValues();
        initialValues.put(KEY_HISTORY_MESSAGE, message);
        initialValues.put(KEY_HISTORY_DATE, date);
        initialValues.put(KEY_HISTORY_GOAL, goalNumber);

        return mDb.insert(HISTORY_TABLE, null, initialValues);

    }

    public Cursor fetchGoalHistory(Long rowID) {

        String where = DBAdapter.KEY_HISTORY_GOAL + "=" + rowID;

        String order = KEY_HISTORY_DATE + " ASC ";

        return mDb.query(HISTORY_TABLE, new String[]{KEY_ROWID, KEY_HISTORY_DATE,
                KEY_HISTORY_MESSAGE, KEY_HISTORY_GOAL}, where, null, null, null, order);
    }

    public Cursor fetchGoalActivities(Long rowID) {

        String where = DBAdapter.KEY_ACTIVITY_GOAL + "=" + rowID;

        Cursor cursor = mDb.query(ACTIVITY_TABLE, new String[]{KEY_ROWID, KEY_ACTIVITY_NUMBER,
                KEY_ACTIVITY_UNIT, KEY_TYPE, KEY_ACTIVITY_DATE, KEY_ACTIVITY_GOAL}, where, null, null, null, null);

        return cursor;
    }

    public double getGoalWalkProgress(long rowID) {

        Cursor cursor = fetchGoalActivities(rowID);

        double totalProgress = 0;

        Cursor goalCursor = fetchGoal(rowID);
        if (goalCursor.moveToFirst()) {
            String walkUnit = goalCursor.getString(goalCursor.getColumnIndexOrThrow(DBAdapter.KEY_GOAL_WALK_UNIT));


            while (cursor.moveToNext()) {
                Boolean climbType = cursor.getInt(cursor.getColumnIndexOrThrow(KEY_TYPE)) > 0;
                if (!climbType) {
                    String unit = cursor.getString(cursor.getColumnIndexOrThrow(KEY_ACTIVITY_UNIT));
                    Integer distance = cursor.getInt(cursor.getColumnIndexOrThrow(KEY_ACTIVITY_NUMBER));
                    double distanceFormat = GlobalVariables.convertUnits(distance, false, walkUnit, unit);
                    totalProgress = totalProgress + distanceFormat;
                }
            }
        }


        return totalProgress;
    }

    public double getGoalClimbProgress(long rowID) {

        Cursor cursor = fetchGoalActivities(rowID);

        double totalProgress = 0;

        Cursor goalCursor = fetchGoal(rowID);
        if (goalCursor.moveToFirst()) {
            String climbUnit = goalCursor.getString(goalCursor.getColumnIndexOrThrow(DBAdapter.KEY_GOAL_CLIMB_UNIT));
            Long calorieId = goalCursor.getLong(goalCursor.getColumnIndexOrThrow(DBAdapter.KEY_GOAL_CALORIE));

            if(calorieId != null){
                climbUnit = "Calories";
            }

            while (cursor.moveToNext()) {
                Boolean climbType = cursor.getInt(cursor.getColumnIndexOrThrow(KEY_TYPE)) > 0;
                if (climbType) {
                    String unit = cursor.getString(cursor.getColumnIndexOrThrow(KEY_ACTIVITY_UNIT));
                    Integer distance = cursor.getInt(cursor.getColumnIndexOrThrow(KEY_ACTIVITY_NUMBER));
                    double distanceFormat = GlobalVariables.convertUnits(distance, true, climbUnit, unit);
                    totalProgress = totalProgress + distanceFormat;
                }
            }
        }


        return totalProgress;
    }

    public String getStatusString(int statusNumber) {
        String statusText = "";
        switch (statusNumber) {
            case GlobalVariables.GOAL_MODE_ACTIVE:
                statusText = "active";
                break;
            case GlobalVariables.GOAL_MODE_PENDING:
                statusText = "pending";
                break;
            case GlobalVariables.GOAL_MODE_ABANDONED:
                statusText = "abandoned";
                break;
            case GlobalVariables.GOAL_MODE_OVERDUE:
                statusText = "overdue";
                break;
            case GlobalVariables.GOAL_MODE_COMPLETED:
                statusText = "completed";
                break;
            case GlobalVariables.GOAL_MODE_SUSPENDED:
                statusText = "suspended";
                break;
        }
        return statusText;
    }

    public boolean updateGoalFacebookId(long rowId, String postId) {
        ContentValues args = new ContentValues();
        args.put(KEY_GOAL_FACEBOOK_ID, postId);
        return mDb.update(GOAL_TABLE, args, KEY_ROWID + "=" + rowId, null) > 0;
    }

    public boolean updateGoalTwitterId(long rowId, Long postId) {
        ContentValues args = new ContentValues();
        args.put(KEY_GOAL_TWITTER_ID, postId);
        return mDb.update(GOAL_TABLE, args, KEY_ROWID + "=" + rowId, null) > 0;
    }

    public Cursor fetchMap(long rowId) throws SQLException {

        Cursor mCursor =

                mDb.query(true, MAP_TABLE, new String[]{KEY_ROWID, KEY_MAP_START_LONG, KEY_MAP_START_LAT, KEY_MAP_END_LAT, KEY_MAP_END_LONG}, KEY_ROWID + "=" + rowId, null,
                        null, null, null, null);
        if (mCursor != null) {
            mCursor.moveToFirst();
        }
        return mCursor;

    }

    public long createMap(Double startLat, Double startLong, Double endLat, Double endLong) {
        ContentValues initialValues = new ContentValues();
        initialValues.put(KEY_MAP_START_LAT, startLat);
        initialValues.put(KEY_MAP_START_LONG, startLong);
        initialValues.put(KEY_MAP_END_LAT, endLat);
        initialValues.put(KEY_MAP_END_LONG, endLong);


        Long rowID = mDb.insert(MAP_TABLE, null, initialValues);

        return rowID;
    }

    public void updateGoalMap(long goalId, long mapId) {
        ContentValues args = new ContentValues();
        args.put(KEY_GOAL_MAP, mapId);
        mDb.update(GOAL_TABLE, args, KEY_ROWID + "=" + goalId, null);
    }

    public void deleteMap(long goalId) {
        Cursor goalCursor = fetchGoal(goalId);
        if (goalCursor.moveToFirst()) {
            Long mapId = goalCursor.getLong(goalCursor.getColumnIndexOrThrow(DBAdapter.KEY_GOAL_MAP));

            if (mapId != null) {
                ContentValues args = new ContentValues();
                args.putNull(KEY_GOAL_MAP);
                mDb.update(GOAL_TABLE, args, KEY_ROWID + "=" + goalId, null);
                mDb.delete(MAP_TABLE, KEY_ROWID + "=" + mapId, null);
            }
        }
    }

    public long insertTreat(String name, Bitmap image, int calories) {

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        image.compress(Bitmap.CompressFormat.PNG, 0, outputStream);
        byte[] data = outputStream.toByteArray();

        ContentValues initialValues = new ContentValues();
        initialValues.put(KEY_TREAT_IMAGE, data);
        initialValues.put(KEY_TREAT_NAME, name);
        initialValues.put(KEY_TREAT_CALORIES, calories);


        Long rowID = mDb.insert(TREAT_TABLE, null, initialValues);

        return rowID;
    }

    public long insertTreat(String name, byte[] image, int calories) {

        ContentValues initialValues = new ContentValues();
        initialValues.put(KEY_TREAT_IMAGE, image);
        initialValues.put(KEY_TREAT_NAME, name);
        initialValues.put(KEY_TREAT_CALORIES, calories);


        Long rowID = mDb.insert(TREAT_TABLE, null, initialValues);

        return rowID;
    }

    public Cursor fetchTreat(Long rowId) {

        Cursor mCursor =

                mDb.query(true, TREAT_TABLE, new String[]{KEY_ROWID, KEY_TREAT_NAME, KEY_TREAT_CALORIES, KEY_TREAT_IMAGE}, KEY_ROWID + "=" + rowId, null,
                        null, null, null, null);
        if (mCursor != null) {
            mCursor.moveToFirst();
        }
        return mCursor;
    }

    public Cursor fetchTreats() {

        Cursor mCursor =

                mDb.query(true, TREAT_TABLE, new String[]{KEY_ROWID, KEY_TREAT_NAME, KEY_TREAT_CALORIES, KEY_TREAT_IMAGE}, null, null,
                        null, null, null, null);
        if (mCursor != null) {
            mCursor.moveToFirst();
        }
        return mCursor;
    }

    public void connectTreat(Long goalId, Long treatId){
        ContentValues args = new ContentValues();
        args.put(KEY_GOAL_CALORIE, treatId);
        mDb.update(GOAL_TABLE, args, KEY_ROWID + "=" + goalId, null);
    }

    public void disconnectTreat(Long goalId){
        ContentValues args = new ContentValues();
        args.putNull(KEY_GOAL_CALORIE);
        mDb.update(GOAL_TABLE, args, KEY_ROWID + "=" + goalId, null);
    }


    public void addGoalChallenge(long goalId, String opponent, int penalty) {
        ContentValues initialValues = new ContentValues();
        initialValues.put(KEY_CHALLENGE_OPPONENT, opponent);
        initialValues.put(KEY_CHALLENGE_PENALTY, penalty);
        initialValues.put(KEY_CHALLENGE_LOST, false);
        initialValues.put(KEY_CHALLENGE_WON, false);


        Long challengeId = mDb.insert(CHALLENGE_TABLE, null, initialValues);

        Log.d("Challenge", challengeId + "");

        ContentValues args = new ContentValues();
        args.put(KEY_GOAL_CHALLENGE, challengeId);
        mDb.update(GOAL_TABLE, args, KEY_ROWID + "=" + goalId, null);
    }

    public Cursor fetchChallengeGoals() {

        String where = DBAdapter.KEY_GOAL_CHALLENGE + " NOT NULL" ;

        String order = KEY_GOAL_COMPLETE + " ASC, " + KEY_GOAL_ACTIVE + " DESC, " + KEY_GOAL_START + " ASC, " +
                KEY_GOAL_END + " ASC ";

        return mDb.query(GOAL_TABLE, new String[]{KEY_ROWID, KEY_GOAL_TITLE, KEY_GOAL_WALK, KEY_GOAL_CLIMB,
                KEY_GOAL_WALK_UNIT, KEY_GOAL_CLIMB_UNIT, KEY_GOAL_START, KEY_GOAL_END, KEY_GOAL_COMPLETE,
                KEY_TYPE, KEY_GOAL_ACTIVE, KEY_GOAL_CALORIE, KEY_GOAL_CHALLENGE}, where, null, null, null, order);
    }

    public Cursor fetchChallenge(Long rowId) {

        Cursor mCursor =

                mDb.query(true, CHALLENGE_TABLE, new String[]{KEY_ROWID, KEY_CHALLENGE_OPPONENT,
                                KEY_CHALLENGE_PENALTY, KEY_CHALLENGE_LOST, KEY_CHALLENGE_WON,
                                KEY_CHALLENGE_FINISH}, KEY_ROWID + "=" + rowId, null,
                        null, null, null, null);
        if (mCursor != null) {
            mCursor.moveToFirst();
        }
        return mCursor;
    }

    public void updateChallenge(Long rowId, boolean lost, boolean won){
        ContentValues args = new ContentValues();
        args.put(KEY_CHALLENGE_LOST, lost);
        args.put(KEY_CHALLENGE_WON, won);
        mDb.update(CHALLENGE_TABLE, args, KEY_ROWID + "=" + rowId, null);
    }

    public void updateChallengeEnd(Long rowId, Long end){
        ContentValues args = new ContentValues();
        args.put(KEY_CHALLENGE_FINISH, end);
        mDb.update(CHALLENGE_TABLE, args, KEY_ROWID + "=" + rowId, null);
    }
}
