package fi.tuni.worktimeapp;

/*
Class to represent work task
 */
public class Work {
    private int workId = 0;
    private int userId = 0;
    private String userName = "";
    private int taskId = 0;
    private String taskName = "";
    private int clientId = 0;
    private String clientName = "";
    private int duration = 0;
    private double  startLocationLongitude = 0;
    private double startLocationLatitude = 0;
    private double endLocationLongitude = 0;
    private double endLocationLatitude = 0;
    private int day;
    private int month;
    private int year;
    private int hour;
    private int minute;
    private int breakAmount = 0;
    private String info = "";

    public Work(int workId ,int userdId, String userName, int taskId, String taskName,
                int clientId, String clientName) {
        this.workId = workId;
        this.userId = userdId;
        this.userName = userName;
        this.taskId = taskId;
        this.taskName = taskName;
        this.clientId = clientId;
        this.clientName = clientName;
    }

    public Work() {

    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getUserName() {
        return userName;
    }

    public void setTaskId(int taskId) {
        this.taskId = taskId;
    }

    public int getTaskId() {
        return taskId;
    }

    public void setTaskName(String taskName) {
        this.taskName = taskName;
    }

    public String getTaskName() {
        return taskName;
    }

    public void setClientId(int clientId) {
        this.clientId = clientId;
    }

    public int getClientId() {
        return clientId;
    }

    public void setClientName(String clientName) {
        this.clientName = clientName;
    }

    public String getClientName() {
        return clientName;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    public int getDuration() {
        return duration;
    }

    public double getStartLocationLongitude() {
        return startLocationLongitude;
    }

    public void setStartLocationLongitude(double startLocationLongitude) {
        this.startLocationLongitude = startLocationLongitude;
    }

    public double getStartLocationLatitude() {
        return startLocationLatitude;
    }

    public void setStartLocationLatitude(double startLocationLatitude) {
        this.startLocationLatitude = startLocationLatitude;
    }

    public double getEndLocationLongitude() {
        return endLocationLongitude;
    }

    public void setEndLocationLongitude(double endLocationLongitude) {
        this.endLocationLongitude = endLocationLongitude;
    }

    public double getEndLocationLatitude() {
        return endLocationLatitude;
    }

    public void setEndLocationLatitude(double endLocationLatitude) {
        this.endLocationLatitude = endLocationLatitude;
    }

    public int getBreakAmount() {
        return breakAmount;
    }

    public void setBreakAmount(int breakAmount) {
        this.breakAmount = breakAmount;
    }

    public int getWorkId() {
        return workId;
    }

    public void setWorkId(int workId) {
        this.workId = workId;
    }

    public String getInfo() {
        return info;
    }

    public void setInfo(String info) {
        this.info = info;
    }

    public int getDay() {
        return day;
    }

    public void setDay(int day) {
        this.day = day;
    }

    public int getMonth() {
        return month;
    }

    public void setMonth(int month) {
        this.month = month;
    }

    public int getYear() {
        return year;
    }

    public void setYear(int year) {
        this.year = year;
    }

    public int getHour() {
        return hour;
    }

    public void setHour(int hour) {
        this.hour = hour;
    }

    public int getMinute() {
        return minute;
    }

    public void setMinute(int minute) {
        this.minute = minute;
    }
}

