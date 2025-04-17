package com.example.lich;

import android.app.DatePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;

import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RadioGroup;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.content.ContextCompat;
import androidx.appcompat.widget.PopupMenu;
import androidx.recyclerview.widget.LinearLayoutManager;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import android.app.AlertDialog;
import android.view.LayoutInflater;
import android.widget.Button;

import android.os.Handler;
import android.os.SystemClock;

import com.example.lich.Database.TaoDatabase;
import com.google.android.material.floatingactionbutton.FloatingActionButton;


import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.textfield.TextInputEditText;

public class MainActivity extends AppCompatActivity {
    private TextView titleTV;
    private TextView dateSelectorTV;
    private TextView duongLichMonthYearTV;
    private TextView duongLichDayTV;
    private TextView duongLichNoteTV;
    private TextView amLichMonthYearTV;
    private TextView amLichDayTV;
    private TextView amLichNoteTV;
    private TextView lunarDayInfoTV;
    private TextView hourInfoTV;
    private TextView currentMonthYearTV;
    private ImageButton prevMonthButton;
    private ImageButton nextMonthButton;
    private TableLayout calendarTable;
    private ImageButton cancelButton;
    private ImageView imageView6;

    private Calendar calendar;
    private SimpleDateFormat monthYearFormat;
    private int currentYear, currentMonth, currentDay;
    private int selectedYear, selectedMonth, selectedDay;
    private boolean isLoggedIn = false;

    // Mảng tên tháng âm lịch
    private final String[] LUNAR_MONTHS = {"", "Giêng", "Hai", "Ba", "Tư", "Năm", "Sáu", "Bảy", "Tám", "Chín", "Mười", "Mười một", "Chạp"};
    private final String[] CAN = {"Giáp", "Ất", "Bính", "Đinh", "Mậu", "Kỷ", "Canh", "Tân", "Nhâm", "Quý"};
    private final String[] CHI = {"Tý", "Sửu", "Dần", "Mão", "Thìn", "Tỵ", "Ngọ", "Mùi", "Thân", "Dậu", "Tuất", "Hợi"};


    private NoteHelper noteHelper;
    private SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());


    private FloatingActionButton floatingActionButton;
    private Handler handler = new Handler();
    private long startTime = 0L, timeInMilliseconds = 0L, timeSwapBuff = 0L, updateTime = 0L;
    private Runnable updateTimerThread;

    private List<String> savedTimesList = new ArrayList<>();


    private int lastClickedYear = -1;
    private int lastClickedMonth = -1;
    private int lastClickedDay = -1;


    private RecyclerView rvEvents;
    private EventAdapter eventAdapter;
    private List<Event> eventList;

    private Handler handler1 = new Handler();
    private Runnable updateCalendarRunnable;

    private TaoDatabase dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.lichchinh);

        dbHelper = new TaoDatabase(this);


        rvEvents = findViewById(R.id.rvEvents);
        rvEvents.setLayoutManager(new LinearLayoutManager(this));

        eventList = new ArrayList<>();
        eventAdapter = new EventAdapter(eventList);
        rvEvents.setAdapter(eventAdapter);
        setupEventClickListeners();



        floatingActionButton = findViewById(R.id.button);
        floatingActionButton.setOnClickListener(v -> showTimerDialog());
        floatingActionButton.setOnClickListener(v -> showFabMenu());

        noteHelper = new NoteHelper(this);

        initializeViews();
        setupCalendar();
        setupListeners();

        Intent intent = getIntent();
        isLoggedIn = intent.getBooleanExtra("isLoggedIn", false);

        imageView6.setOnClickListener(v -> {
            if (isLoggedIn) {
                showLogoutMenu(v);
            } else {
                Intent loginIntent = new Intent(MainActivity.this, Login.class);
                startActivity(loginIntent);
            }
        });

        //Hiện thị sự kiện
        String currentDate = dateFormat.format(Calendar.getInstance().getTime());
        loadEventsForDate(currentDate);

        // Cập nhật lịch
        String currentDateString = String.format(Locale.getDefault(), "%02d-%02d-%d",
                selectedDay, (selectedMonth + 1), selectedYear);
        loadEventsForDate(currentDateString);
    }

    //khi người dùng nhấp vào một sự kiện
    private void setupEventClickListeners() {
        eventAdapter.setOnEventClickListener(event -> showEditEventDialog(event));
    }
    //Chỉnh sửa sự kiện
    private void showEditEventDialog(Event event) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_edit_event, null);
        builder.setView(dialogView);

        TextInputEditText etTitle = dialogView.findViewById(R.id.etTitle);
        TextInputEditText etMieuTa = dialogView.findViewById(R.id.etMieuTa);
        TextInputEditText etDate = dialogView.findViewById(R.id.etDate);
        TextInputEditText etTime = dialogView.findViewById(R.id.etTime);
        TextInputEditText etLocation = dialogView.findViewById(R.id.etLocation);
        RadioGroup rgCalendarType = dialogView.findViewById(R.id.rgCalendarType);
        Button btnUpdate = dialogView.findViewById(R.id.btnUpdate);
        Button btnDelete = dialogView.findViewById(R.id.btnDelete);

        etTitle.setText(event.getTitle());
        etMieuTa.setText(event.getDescription());
        etDate.setText(event.getDate());
        etTime.setText(event.getTime());
        etLocation.setText(event.getLocation());

        if (event.getCalendarType().equals("Lịch Dương")) {
            rgCalendarType.check(R.id.rbDuongLich);
        } else if (event.getCalendarType().equals("Lịch Âm")) {
            rgCalendarType.check(R.id.rbAmLich);
        }

        // Xử lý chọn ngày
        etDate.setOnClickListener(v -> {
            try {
                String[] dateParts = event.getDate().split("-");
                int day = Integer.parseInt(dateParts[0]);
                int month = Integer.parseInt(dateParts[1]) - 1;
                int year = Integer.parseInt(dateParts[2]);

                DatePickerDialog datePickerDialog = new DatePickerDialog(
                        this,
                        (view, selectedYear, selectedMonth, selectedDay) -> {
                            String newSelectedDate = String.format(
                                    Locale.getDefault(),
                                    "%02d-%02d-%d",
                                    selectedDay,
                                    (selectedMonth + 1),
                                    selectedYear
                            );
                            etDate.setText(newSelectedDate);
                        },
                        year,
                        month,
                        day
                );
                datePickerDialog.show();
            } catch (Exception e) {
                // Xử lý nếu định dạng ngày không đúng
                DatePickerDialog datePickerDialog = new DatePickerDialog(
                        this,
                        (view, selectedYear, selectedMonth, selectedDay) -> {
                            String newSelectedDate = String.format(
                                    Locale.getDefault(),
                                    "%02d-%02d-%d",
                                    selectedDay,
                                    (selectedMonth + 1),
                                    selectedYear
                            );
                            etDate.setText(newSelectedDate);
                        },
                        selectedYear,
                        selectedMonth,
                        selectedDay
                );
                datePickerDialog.show();
            }
        });

        AlertDialog dialog = builder.create();

        btnUpdate.setOnClickListener(v -> {
            String title = etTitle.getText().toString().trim();
            String note = etMieuTa.getText().toString().trim();
            String date = etDate.getText().toString().trim();
            String time = etTime.getText().toString().trim();
            String location = etLocation.getText().toString().trim();

            String calendarType;
            int selectedRadioButtonId = rgCalendarType.getCheckedRadioButtonId();
            if (selectedRadioButtonId == R.id.rbDuongLich) {
                calendarType = "Lịch Dương";
            } else if (selectedRadioButtonId == R.id.rbAmLich) {
                calendarType = "Lịch Âm";
            } else {
                Toast.makeText(this, "Vui lòng chọn loại lịch", Toast.LENGTH_SHORT).show();
                return;
            }

            if (title.isEmpty()) {
                etTitle.setError("Vui lòng nhập tiêu đề");
                return;
            }

            if (date.isEmpty() || !date.matches("\\d{2}-\\d{2}-\\d{4}")) {
                etDate.setError("Vui lòng nhập ngày hợp lệ");
                return;
            }

            boolean result = dbHelper.updateEvent(
                    event.getId(), title, note, date, time, location, calendarType);

            if (result) {
                loadEventsForDate(date);
                Toast.makeText(this, "Đã cập nhật sự kiện", Toast.LENGTH_SHORT).show();
                dialog.dismiss();
            } else {
                Toast.makeText(this, "Lỗi khi cập nhật sự kiện", Toast.LENGTH_SHORT).show();
            }
        });

        btnDelete.setOnClickListener(v -> {
            new AlertDialog.Builder(this)
                    .setTitle("Xác nhận xóa")
                    .setMessage("Bạn có chắc chắn muốn xóa sự kiện này?")
                    .setPositiveButton("Xóa", (dialogInterface, i) -> {
                        boolean result = dbHelper.deleteEvent(event.getId());
                        if (result) {
                            loadEventsForDate(event.getDate());
                            Toast.makeText(this, "Đã xóa sự kiện", Toast.LENGTH_SHORT).show();
                            dialog.dismiss();
                        } else {
                            Toast.makeText(this, "Lỗi khi xóa sự kiện", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .setNegativeButton("Hủy", null)
                    .show();
        });

        dialog.show();
    }

    // Thêm sự kiện
    private void showAddEventDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_event, null);
        builder.setView(dialogView);

        TextInputEditText etTitle = dialogView.findViewById(R.id.etTitle);
        TextInputEditText etMieuTa = dialogView.findViewById(R.id.etMieuTa);
        TextInputEditText etDate = dialogView.findViewById(R.id.etDate);
        TextInputEditText etTime = dialogView.findViewById(R.id.etTime);
        TextInputEditText etLocation = dialogView.findViewById(R.id.etLocation);
        RadioGroup rgCalendarType = dialogView.findViewById(R.id.rgCalendarType);
        Button btnSave = dialogView.findViewById(R.id.btnSave);

        // Nếu đã chọn ngày trước đó, hiển thị ngày đó
        String selectedDateString = String.format(Locale.getDefault(), "%02d-%02d-%d",
                selectedDay, (selectedMonth + 1), selectedYear);
        etDate.setText(selectedDateString);

        // Sự kiện click để chọn ngày
        etDate.setOnClickListener(v -> {
            DatePickerDialog datePickerDialog = new DatePickerDialog(
                    this,
                    (view, year, monthOfYear, dayOfMonth) -> {
                        // Cập nhật ngày được chọn
                        selectedYear = year;
                        selectedMonth = monthOfYear;
                        selectedDay = dayOfMonth;

                        // Hiển thị ngày đã chọn
                        String newSelectedDate = String.format(
                                Locale.getDefault(),
                                "%02d-%02d-%d",
                                dayOfMonth,
                                (monthOfYear + 1),
                                year
                        );
                        etDate.setText(newSelectedDate);
                    },
                    selectedYear,
                    selectedMonth,
                    selectedDay
            );
            datePickerDialog.show();
        });

        //người dùng nhập ngày
        etDate.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                // Kiểm tra định dạng ngày khi người dùng nhập
                String dateText = s.toString().trim();
                if (dateText.matches("\\d{2}-\\d{2}-\\d{4}")) {
                    try {
                        String[] dateParts = dateText.split("-");
                        int day = Integer.parseInt(dateParts[0]);
                        int month = Integer.parseInt(dateParts[1]) - 1;
                        int year = Integer.parseInt(dateParts[2]);

                        // Cập nhật ngày đã chọn
                        selectedDay = day;
                        selectedMonth = month;
                        selectedYear = year;
                    } catch (Exception e) {
                        etDate.setError("Định dạng ngày không hợp lệ");
                    }
                }
            }
        });

        AlertDialog dialog = builder.create();

        btnSave.setOnClickListener(v -> {
            String title = etTitle.getText().toString().trim();
            String note = etMieuTa.getText().toString().trim();
            String date = etDate.getText().toString().trim();
            String time = etTime.getText().toString().trim();
            String location = etLocation.getText().toString().trim();

            String calendarType;
            int selectedRadioButtonId = rgCalendarType.getCheckedRadioButtonId();
            if (selectedRadioButtonId == R.id.rbDuongLich) {
                calendarType = "Lịch Dương";
            } else if (selectedRadioButtonId == R.id.rbAmLich) {
                calendarType = "Lịch Âm";
            } else {
                Toast.makeText(this, "Vui lòng chọn loại lịch", Toast.LENGTH_SHORT).show();
                return;
            }

            if (title.isEmpty()) {
                etTitle.setError("Vui lòng nhập tiêu đề");
                return;
            }

            if (date.isEmpty() || !date.matches("\\d{2}-\\d{2}-\\d{4}")) {
                etDate.setError("Vui lòng nhập ngày hợp lệ");
                return;
            }

            long result = dbHelper.insertEvent(title, note, date, time, location, calendarType);

            if (result != -1) {
                loadEventsForDate(date);


                if (calendarType.equals("Lịch Dương")) {
                    TextView duongLichNoteTV = findViewById(R.id.duongLichNoteTV);
                    duongLichNoteTV.setText(title);
                } else if (calendarType.equals("Lịch Âm")) {
                    TextView amLichNoteTV = findViewById(R.id.amLichNoteTV);
                    amLichNoteTV.setText(title);
                }

                Toast.makeText(this, "Đã lưu sự kiện", Toast.LENGTH_SHORT).show();
                dialog.dismiss();
            } else {
                Toast.makeText(this, "Lỗi khi lưu sự kiện", Toast.LENGTH_SHORT).show();
            }
        });

        updateCalendarTable(selectedYear, selectedMonth);
        dialog.show();
    }

    private void loadEventsForDate(String date) {
        List<Event> duongEvents = dbHelper.getEventsByDateAndCalendarType(date, "Lịch Dương");
        List<Event> amEvents = dbHelper.getEventsByDateAndCalendarType(date, "Lịch Âm");

        TextView duongLichNoteTV = findViewById(R.id.duongLichNoteTV);
        TextView amLichNoteTV = findViewById(R.id.amLichNoteTV);

        duongLichNoteTV.setText("");
        amLichNoteTV.setText("");

        if (!duongEvents.isEmpty()) {
            StringBuilder duongText = new StringBuilder();
            for (Event event : duongEvents) {
                duongText.append(event.getTitle()).append("\n");
            }
            duongLichNoteTV.setText(duongText.toString().trim());
        }

        if (!amEvents.isEmpty()) {
            StringBuilder amText = new StringBuilder();
            for (Event event : amEvents) {
                amText.append(event.getTitle()).append("\n");
            }
            amLichNoteTV.setText(amText.toString().trim());
        }

        eventList.clear();
        eventList.addAll(duongEvents);
        eventList.addAll(amEvents);
        eventAdapter.notifyDataSetChanged();
    }

    //Menu dấu cộng
    private void showFabMenu() {
        PopupMenu popupMenu = new PopupMenu(this, floatingActionButton);
        popupMenu.getMenuInflater().inflate(R.menu.menucong, popupMenu.getMenu());

        popupMenu.setOnMenuItemClickListener(item -> {
            int id = item.getItemId();
            if (id == R.id.menu_clock) {
                showTimerDialog();
                return true;
            } else if (id == R.id.menu_add_event) {
                showAddEventDialog();
                return true;
            } else {
                return false;
            }
        });
        popupMenu.show();
    }


    // Đồng hồ
    private void showTimerDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_timer, null);
        builder.setView(dialogView);

        AnalogClockView analogClock = dialogView.findViewById(R.id.analogClock);
        TextView timerTextView = dialogView.findViewById(R.id.timerTextView);
        TextView realTimeTextView = dialogView.findViewById(R.id.realTimeTextView);
        TextView savedTimesTextView = dialogView.findViewById(R.id.savedTimesTextView);
        Button startButton = dialogView.findViewById(R.id.startButton);
        Button stopButton = dialogView.findViewById(R.id.stopButton);
        Button resumeButton = dialogView.findViewById(R.id.resumeButton);
        Button resetButton = dialogView.findViewById(R.id.resetButton);

        savedTimesList = new ArrayList<>();
        updateSavedTimesDisplay(savedTimesTextView);

        // Cập nhật thời gian thực
        analogClock.setTimeUpdateListener(formattedTime -> realTimeTextView.setText(formattedTime));

        // Xử lý cập nhật timer
        updateTimerThread = new Runnable() {
            public void run() {
                timeInMilliseconds = SystemClock.uptimeMillis() - startTime;
                updateTime = timeSwapBuff + timeInMilliseconds;
                updateTimerDisplay(timerTextView, updateTime);
                handler.postDelayed(this, 0);
            }
        };

        // Xử lý nút Start
        startButton.setOnClickListener(v -> {
            resetTimer(timerTextView);
            handler.postDelayed(updateTimerThread, 0);
            toggleTimerButtons(startButton, stopButton, resumeButton, false, true, false);
        });

        // Xử lý nút Stop
        stopButton.setOnClickListener(v -> {
            timeSwapBuff += timeInMilliseconds;
            handler.removeCallbacks(updateTimerThread);
            toggleTimerButtons(startButton, stopButton, resumeButton, true, false, true);

            // Thêm thời gian hiện tại vào lịch sử
            String currentTime = timerTextView.getText().toString();
            savedTimesList.add(currentTime);
            updateSavedTimesDisplay(savedTimesTextView);
        });

        // Xử lý nút Resume
        resumeButton.setOnClickListener(v -> {
            startTime = SystemClock.uptimeMillis();
            handler.postDelayed(updateTimerThread, 0);
            toggleTimerButtons(startButton, stopButton, resumeButton, false, true, false);
        });

        // Xử lý nút Reset
        resetButton.setOnClickListener(v -> {
            savedTimesList.clear();
            updateSavedTimesDisplay(savedTimesTextView);
        });

        AlertDialog dialog = builder.create();
        dialog.setOnDismissListener(dialogInterface -> {
            handler.removeCallbacks(updateTimerThread);  // Dừng handler khi dialog đóng
        });
        dialog.show();
    }

    // Các phương thức hỗ trợ
    private void updateTimerDisplay(TextView textView, long timeInMillis) {
        int secs = (int) (timeInMillis / 1000);
        int mins = secs / 60;
        int hrs = mins / 60;
        secs %= 60;
        mins %= 60;
        textView.setText(String.format(Locale.getDefault(), "%02d:%02d:%02d", hrs, mins, secs));
    }

    private void updateSavedTimesDisplay(TextView textView) {
        StringBuilder sb = new StringBuilder();
        for (String time : savedTimesList) {
            sb.append("• ").append(time).append("\n");
        }
        textView.setText(sb.toString());
    }

    private void resetTimer(TextView textView) {
        timeSwapBuff = 0;
        startTime = SystemClock.uptimeMillis();
        handler.removeCallbacks(updateTimerThread);
        updateTimerDisplay(textView, 0);
    }

    private void toggleTimerButtons(Button start, Button stop, Button resume,
                                    boolean startState, boolean stopState, boolean resumeState) {
        start.setEnabled(startState);
        stop.setEnabled(stopState);
        resume.setEnabled(resumeState);

        // Thay đổi màu nút theo trạng thái
        int activeColor = ContextCompat.getColor(this, R.color.colorPrimary);
        int inactiveColor = ContextCompat.getColor(this, R.color.gray);

        start.setBackgroundColor(startState ? activeColor : inactiveColor);
        stop.setBackgroundColor(stopState ? activeColor : inactiveColor);
        resume.setBackgroundColor(resumeState ? activeColor : inactiveColor);
    }

    //ghi chú
    private static class NoteHelper {
        private TaoDatabase database;
        private Context context;

        public NoteHelper(Context context) {
            this.context = context;
            this.database = new TaoDatabase(context);
        }

        public void saveNote(String date, String note, String title, String time) {
            // Kiểm tra xem ghi chú có tồn tại cho ngày này không
            SQLiteDatabase db = database.getReadableDatabase();
            String selection = TaoDatabase.TB_Note_Ngay + " = ?";
            String[] selectionArgs = {date};
            Cursor cursor = db.query(TaoDatabase.TB_Note, new String[]{TaoDatabase.TB_Note_MaNote},
                    selection, selectionArgs, null, null, null);

            if (cursor.moveToFirst()) {
                // Cập nhật ghi chú hiện có
                int noteId = cursor.getInt(cursor.getColumnIndexOrThrow(TaoDatabase.TB_Note_MaNote));
                database.updateNote(noteId, title, note, date, time);
            } else {
                // Chèn ghi chú mới
                database.insertNote(title, note, date, time);
            }
            cursor.close();
        }

        public String getNote(String date) {
            Cursor cursor = database.getNoteByDate(date);
            String note = "";
            if (cursor.moveToFirst()) {
                note = cursor.getString(cursor.getColumnIndexOrThrow(TaoDatabase.TB_Note_NoiDung));
            }
            cursor.close();
            return note;
        }

        public String getNoteTitle(String date) {
            Cursor cursor = database.getNoteByDate(date);
            String title = "";
            if (cursor.moveToFirst()) {
                title = cursor.getString(cursor.getColumnIndexOrThrow(TaoDatabase.TB_Note_TieuDe));
            }
            cursor.close();
            return title;
        }

        public void deleteNote(String date) {
            database.deleteNoteByDate(date);
        }
    }

    //Menu đăng xuất
    private void showLogoutMenu(View view) {
        PopupMenu popupMenu = new PopupMenu(this, view);
        popupMenu.getMenuInflater().inflate(R.menu.menu_logout, popupMenu.getMenu());

        popupMenu.setOnMenuItemClickListener(item -> {
            if (item.getItemId() == R.id.menu_logout) {
                logout();
                return true;
            }
            return false;
        });
        popupMenu.show();
    }

    private void logout() {
        isLoggedIn = false;
        Toast.makeText(MainActivity.this, "Đã đăng xuất", Toast.LENGTH_SHORT).show();
    }

    private void initializeViews() {
        titleTV = findViewById(R.id.titleTV);
        dateSelectorTV = findViewById(R.id.dateSelectorTV);
        duongLichMonthYearTV = findViewById(R.id.duongLichMonthYearTV);
        duongLichDayTV = findViewById(R.id.duongLichDayTV);
        duongLichNoteTV = findViewById(R.id.duongLichNoteTV);
        amLichMonthYearTV = findViewById(R.id.amLichMonthYearTV);
        amLichDayTV = findViewById(R.id.amLichDayTV);
        amLichNoteTV = findViewById(R.id.amLichNoteTV);
        lunarDayInfoTV = findViewById(R.id.lunarDayInfoTV);
        hourInfoTV = findViewById(R.id.hourInfoTV);
        currentMonthYearTV = findViewById(R.id.currentMonthYearTV);
        prevMonthButton = findViewById(R.id.prevMonthButton);
        nextMonthButton = findViewById(R.id.nextMonthButton);
        calendarTable = findViewById(R.id.calendarTable);
        cancelButton = findViewById(R.id.cancelButton);
        imageView6 = findViewById(R.id.imageView6);
    }

    private void setupCalendar() {
        calendar = Calendar.getInstance();// Lấy ngày hiện tại
        monthYearFormat = new SimpleDateFormat("MMMM yyyy", new Locale("vi"));
        currentYear = calendar.get(Calendar.YEAR);
        currentMonth = calendar.get(Calendar.MONTH);
        currentDay = calendar.get(Calendar.DAY_OF_MONTH);

        updateDateInfo(currentYear, currentMonth, currentDay);
        updateCalendarTable(currentYear, currentMonth);
        // Hiện thị tháng và năm hiện tại khi run app
        currentMonthYearTV.setText(String.format("Tháng %d, %d", currentMonth + 1, currentYear));
    }

    private void setupListeners() {
        dateSelectorTV.setOnClickListener(v -> {
            Toast.makeText(MainActivity.this, "Chưa biết làm sao", Toast.LENGTH_SHORT).show();
        });

        prevMonthButton.setOnClickListener(v -> {
            calendar.add(Calendar.MONTH, -1);
            currentYear = calendar.get(Calendar.YEAR);
            currentMonth = calendar.get(Calendar.MONTH);
            updateCalendarTable(currentYear, currentMonth);
            currentMonthYearTV.setText(String.format("Tháng %d, %d", currentMonth + 1, currentYear));
        });

        nextMonthButton.setOnClickListener(v -> {
            calendar.add(Calendar.MONTH, 1);
            currentYear = calendar.get(Calendar.YEAR);
            currentMonth = calendar.get(Calendar.MONTH);
            updateCalendarTable(currentYear, currentMonth);
            currentMonthYearTV.setText(String.format("Tháng %d, %d", currentMonth + 1, currentYear));
        });
    }




    //Tham khảo tính lịch âm dựa theo thuật toán của Hồ Ngọc Đức
    private static final double TIME_ZONE = 7.0;
    private int[] convertSolar2Lunar(int dd, int mm, int yy) {
        int lunarDay, lunarMonth, lunarYear, lunarLeap;
        long k, dayNumber, monthStart, a11, b11, diff, leapMonthDiff;

        dayNumber = jdFromDate(dd, mm, yy);
        k = (long) Math.floor((dayNumber - 2415021.076998695) / 29.530588853);
        monthStart = getNewMoonDay(k + 1, TIME_ZONE);
        if (monthStart > dayNumber) {
            monthStart = getNewMoonDay(k, TIME_ZONE);
        }

        a11 = getLunarMonth11(yy, TIME_ZONE);
        b11 = a11;
        if (a11 >= monthStart) {
            lunarYear = yy;
            a11 = getLunarMonth11(yy - 1, TIME_ZONE);
        } else {
            lunarYear = yy + 1;
            b11 = getLunarMonth11(yy + 1, TIME_ZONE);
        }

        lunarDay = (int) (dayNumber - monthStart + 1);
        diff = (long) Math.floor((monthStart - a11) / 29);
        lunarLeap = 0;
        leapMonthDiff = getLeapMonthOffset(a11, TIME_ZONE);

        if (diff >= 4 && diff <= 14) {
            if (leapMonthDiff >= 0) {
                if (diff == leapMonthDiff - 1) {
                    lunarMonth = (int) (diff + 1);
                    lunarLeap = 1;
                } else if (diff == leapMonthDiff) {
                    lunarMonth = (int) diff;
                    lunarLeap = 0;
                    diff = leapMonthDiff - 1;
                } else {
                    lunarMonth = (int) (diff + 1);
                }
            } else {
                lunarMonth = (int) (diff + 1);
            }
        } else {
            lunarMonth = (int) (diff + 1);
        }

        if (lunarMonth > 12) {
            lunarMonth -= 12;
        }
        if (lunarMonth >= 11 && diff < 4) {
            lunarYear -= 1;
        }

        return new int[]{lunarDay, lunarMonth, lunarYear, lunarLeap};
    }
    private long getNewMoonDay(long k, double timeZone) {
        double T, T2, dr, Jd1, M, Mpr, F, C1, deltat, JdNew;
        T = k / 1236.85;
        T2 = T * T;
        dr = Math.PI / 180;
        Jd1 = 2415020.75933 + 29.53058868 * k + 0.0001178 * T2 - 0.000000155 * T * T2;
        Jd1 = Jd1 + 0.00033 * Math.sin((166.56 + 132.87 * T - 0.009173 * T2) * dr);
        M = 359.2242 + 29.10535608 * k - 0.0000333 * T2 - 0.00000347 * T * T2;
        Mpr = 306.0253 + 385.81691806 * k + 0.0107306 * T2 + 0.00001236 * T * T2;
        F = 21.2964 + 390.67050646 * k - 0.0016528 * T2 - 0.00000239 * T * T2;
        C1 = (0.1734 - 0.000393 * T) * Math.sin(M * dr) + 0.0021 * Math.sin(2 * dr * M);
        C1 = C1 - 0.4068 * Math.sin(Mpr * dr) + 0.0161 * Math.sin(dr * 2 * Mpr);
        C1 = C1 - 0.0004 * Math.sin(dr * 3 * Mpr);
        C1 = C1 + 0.0104 * Math.sin(dr * 2 * F) - 0.0051 * Math.sin(dr * (M + Mpr));
        C1 = C1 - 0.0074 * Math.sin(dr * (M - Mpr)) + 0.0004 * Math.sin(dr * (2 * F + M));
        C1 = C1 - 0.0004 * Math.sin(dr * (2 * F - M)) - 0.0006 * Math.sin(dr * (2 * F + Mpr));
        C1 = C1 + 0.0010 * Math.sin(dr * (2 * F - Mpr)) + 0.0005 * Math.sin(dr * (2 * Mpr + M));
        deltat = 0;
        if (T < -11) {
            deltat = 0.001 + 0.000839 * T + 0.0002261 * T2 - 0.00000845 * T * T2 - 0.000000081 * T * T * T2;
        }
        JdNew = Jd1 + C1 - deltat;
        return (long) Math.floor(JdNew + 0.5 + timeZone / 24);
    }
    private long getLunarMonth11(int yy, double timeZone) {
        long k, off, nm, sunLong;
        off = jdFromDate(31, 12, yy) - 2415021;
        k = (long) Math.floor(off / 29.530588853);
        nm = getNewMoonDay(k, timeZone);
        sunLong = (long) Math.floor(getSunLongitude(nm, timeZone));
        if (sunLong >= 9) {
            nm = getNewMoonDay(k - 1, timeZone);
        }
        return nm;
    }
    private long getLeapMonthOffset(long a11, double timeZone) {
        long k, last, arc, i;
        k = (long) Math.floor((a11 - 2415021.076998695) / 29.530588853 + 0.5);
        last = 0;
        i = 1;
        arc = (long) Math.floor(getSunLongitude(getNewMoonDay(k + i, timeZone), timeZone));
        do {
            last = arc;
            i++;
            arc = (long) Math.floor(getSunLongitude(getNewMoonDay(k + i, timeZone), timeZone));
        } while (arc != last && i < 14);
        return i - 1;
    }
    private double getSunLongitude(long jdn, double timeZone) {
        double T, T2, dr, M, L0, DL, L;
        T = (jdn - 2451545.5 - timeZone / 24) / 36525;
        T2 = T * T;
        dr = Math.PI / 180;
        M = 357.52910 + 35999.05030 * T - 0.0001559 * T2 - 0.00000048 * T * T2;
        L0 = 280.46645 + 36000.76983 * T + 0.0003032 * T2;
        DL = (1.914600 - 0.004817 * T - 0.000014 * T2) * Math.sin(dr * M);
        DL = DL + (0.019993 - 0.000101 * T) * Math.sin(dr * 2 * M) + 0.000290 * Math.sin(dr * 3 * M);
        L = L0 + DL;
        L = L * dr;
        L = L - Math.PI * 2 * (Math.floor(L / (Math.PI * 2)));
        return (L / Math.PI * 6);
    }

    // Phương thức lấy Can Chi của năm, tháng, ngày
    private String getCanChiYear(int year) {
        return CAN[(year + 6) % 10] + " " + CHI[(year + 8) % 12];
    }
    private String getCanChiMonth(int lunarMonth, int lunarYear) {
        int canIndex = (lunarYear * 12 + lunarMonth + 3) % 10;
        return CAN[canIndex] + " " + CHI[(lunarMonth + 1) % 12];
    }
    private String getCanChiDay(int jd) {
        int canIndex = (jd + 9) % 10;
        int chiIndex = (jd + 1) % 12;
        return CAN[canIndex] + " " + CHI[chiIndex];
    }

    // Phương thức lấy giờ hoàng đạo
    private String getAuspiciousHours(int lunarDay, int lunarMonth, int lunarYear) {
        String[] ZODIAC_HOURS = {
                "23-1 (Tý), 11-13 (Ngọ)",
                "19-21 (Thân), 3-5 (Dần)",
                "23-1 (Tý), 3-5 (Dần)",
                "5-7 (Mão), 15-17 (Thân)",
                "7-9 (Thìn), 19-21 (Dậu)",
                "9-11 (Tỵ), 21-23 (Tuất)",
                "11-13 (Ngọ), 23-1 (Hợi)",
                "1-3 (Sửu), 13-15 (Mùi)",
                "5-7 (Mão), 17-19 (Dậu)",
                "7-9 (Thìn), 19-21 (Tuất)",
                "9-11 (Tỵ), 21-23 (Hợi)",
                "1-3 (Sửu), 13-15 (Mùi)"
        };
        int jd = (int) jdFromDate(lunarDay, lunarMonth, lunarYear);
        int chiIndex = (jd + 1) % 12;
        return "Giờ hoàng đạo: " + ZODIAC_HOURS[chiIndex];
    }

    // Phương thức chuyển đổi từ dương lịch sang âm lịch
    // Trả về mảng ngày, tháng, năm âm lịch
    private int[] convertSolar2Lunar(int dd, int mm, int yy, double timeZone) {
        int[] lunarDate = new int[3];
        mm = mm - 1;
        double jd = jdFromDate(dd, mm, yy);

        lunarDate[0] = (int)(jd + 1.5) % 30 + 1;
        lunarDate[1] = (((int)(jd + 1.5) / 30) % 12) + 1;
        lunarDate[2] = (int)(jd + 1.5) / 365 + 1;

        // Nếu ngày âm lịch nhỏ hơn ngày dương lịch thì tháng âm lịch trừ 1
        lunarDate[2] = yy - 1;

        return lunarDate;
    }

    // Phương thức tính Julian day từ ngày tháng dương lịch
    private long jdFromDate(int dd, int mm, int yy) {
        long a = (14 - mm) / 12;
        long y = yy + 4800 - a;
        long m = mm + 12 * a - 3;
        return dd + (153 * m + 2)/5 + 365 * y + y/4 - y/100 + y/400 - 32045;
    }




    // Thông tin ngày
    private void updateDateInfo(int year, int month, int day) {
        // Cập nhật dương lịch
        calendar.set(year, month, day);
        duongLichMonthYearTV.setText(String.format("Tháng %d năm %d", month + 1, year));
        duongLichDayTV.setText(String.valueOf(day));
        // Chuyển đổi sang âm lịch
        int[] lunarDate = convertSolar2Lunar(day, month + 1, year);
        int lunarDay = lunarDate[0];
        int lunarMonth = lunarDate[1];
        int lunarYear = lunarDate[2];
        int isLeap = lunarDate[3];
        // Hiển thị thông tin âm lịch
        String canChiYear = getCanChiYear(lunarYear);
        String canChiMonth = getCanChiMonth(lunarMonth, lunarYear);
        String canChiDay = getCanChiDay((int) jdFromDate(day, month + 1, year));
        amLichMonthYearTV.setText(String.format("Tháng %d%s năm %s", lunarMonth, (isLeap == 1 ? " (nhuận)" : ""), canChiYear));
        amLichDayTV.setText(String.valueOf(lunarDay));
        lunarDayInfoTV.setText(String.format("Ngày %s, tháng %s, năm %s", canChiDay, canChiMonth, canChiYear));
        // Hiển thị giờ hoàng đạo
        hourInfoTV.setText(getAuspiciousHours(lunarDay, lunarMonth, lunarYear));

        // Hiển thị sự kiện
        String selectedDate = String.format(Locale.getDefault(), "%02d-%02d-%d", day, month + 1, year);
        loadEventsForDate(selectedDate);
    }

    //Ghi Chú
    private void showNoteDialog(int year, int month, int day) {
        Calendar selectedDate = Calendar.getInstance();
        selectedDate.set(year, month, day);
        String dateKey = dateFormat.format(selectedDate.getTime());

        // Lấy thời gian hiện tại
        SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
        String currentTime = timeFormat.format(new Date());

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_note, null);

        EditText etNote = dialogView.findViewById(R.id.etNote);
        EditText etTitle = dialogView.findViewById(R.id.etTitle);
        Button btnSave = dialogView.findViewById(R.id.btnSave);
        Button btnDelete = dialogView.findViewById(R.id.btnDelete);

        // Lấy nội dung ghi chú hiện có
        String existingNote = noteHelper.getNote(dateKey);
        etNote.setText(existingNote);

        // Lấy tiêu đề ghi chú hiện có
        String existingTitle = noteHelper.getNoteTitle(dateKey);
        etTitle.setText(existingTitle);

        builder.setView(dialogView);
        AlertDialog dialog = builder.create();

        btnSave.setOnClickListener(v -> {
            String note = etNote.getText().toString();
            String title = etTitle.getText().toString();

            // Lưu ghi chú với tiêu đề, nội dung, ngày và giờ
            noteHelper.saveNote(dateKey, note, title, currentTime);
            updateCalendarTable(year, month);
            dialog.dismiss();
        });

        btnDelete.setOnClickListener(v -> {
            noteHelper.deleteNote(dateKey);
            updateCalendarTable(year, month);
            dialog.dismiss();
        });

        dialog.show();
    }


    //cập nhật highlight mà không recreate toàn bộ bảng
    private void updateCalendarHighlight() {
        // Chỉ cập nhật các cell thay vì xóa và tạo lại toàn bộ
        for (int i = 1; i < calendarTable.getChildCount(); i++) {
            TableRow row = (TableRow) calendarTable.getChildAt(i);
            for (int j = 0; j < row.getChildCount(); j++) {
                TextView cell = (TextView) row.getChildAt(j);
                // Cập nhật highlight dựa trên selectedDay, selectedMonth, selectedYear
                handler1.removeCallbacks(updateCalendarRunnable);
                updateCalendarRunnable = () -> {
                    updateCalendarTable(currentYear, currentMonth);
                    updateDateInfo(selectedYear, selectedMonth, selectedDay);
                };
                handler1.postDelayed(updateCalendarRunnable, 200);
            }
        }
    }

    //Bảng lịch
    private void updateCalendarTable(int year, int month) {
        // Xóa tất cả các hàng trừ hàng tiêu đề
        int childCount = calendarTable.getChildCount();
        if (childCount > 1) {
            calendarTable.removeViews(1, childCount - 1);
        }

        Calendar cal = Calendar.getInstance();
        cal.set(year, month, 1);
        int firstDayOfMonth = cal.get(Calendar.DAY_OF_WEEK);
        if (firstDayOfMonth == Calendar.SUNDAY) {
            firstDayOfMonth = 7;
        } else {
            firstDayOfMonth -= 1;
        }
        int daysInMonth = cal.getActualMaximum(Calendar.DAY_OF_MONTH);
        int date = 1;
        int position = 1;
        // Tạo các hàng cho các ngày trong tháng
        while (date <= daysInMonth) {
            TableRow row = new TableRow(this);
            row.setLayoutParams(new TableLayout.LayoutParams(
                    TableLayout.LayoutParams.MATCH_PARENT,
                    TableLayout.LayoutParams.WRAP_CONTENT));
            // Tạo 7 ô cho 7 ngày trong tuần
            for (int i = 1; i <= 7; i++) {
                TextView cell = new TextView(this);
                cell.setPadding(8, 16, 8, 16);
                cell.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
                // Hiển thị ngày trước ngày đầu tiên của tháng
                if (position < firstDayOfMonth && date == 1) {
                    cell.setText("");
                } else if (date <= daysInMonth) {
                    final int selectedDay = date;
                    final int selectedMonth = month;
                    final int selectedYear = year;

                    // Ghi chú
                    Calendar tempCal = Calendar.getInstance();
                    tempCal.set(selectedYear, selectedMonth, selectedDay);
                    String dateKey = dateFormat.format(tempCal.getTime());
                    String note = noteHelper.getNote(dateKey);

                    // Kiểm tra nếu có sự kiện vào ngày này
                    boolean hasEvents = dbHelper.hasEventsOnDate(dateKey);

                    // Hiển thị ghi chú
                    if (!note.isEmpty()) {
                        cell.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.ic_note, 0, 0);
                        cell.setTextColor(ContextCompat.getColor(this, R.color.note_color));
                    }

                    // Nếu có sự kiện, tô màu đỏ cho ngày đó
                    if (hasEvents) {
                        cell.setTextColor(Color.RED);
                    }

                    // Hiển thị ngày hiện tại và ngày được chọn
                    Calendar today = Calendar.getInstance();
                    if (selectedDay == today.get(Calendar.DAY_OF_MONTH) &&
                            selectedMonth == today.get(Calendar.MONTH) &&
                            selectedYear == today.get(Calendar.YEAR)) {
                        cell.setBackgroundResource(R.drawable.current_day_border);
                    } else if (selectedDay == MainActivity.this.selectedDay &&
                            selectedMonth == MainActivity.this.selectedMonth &&
                            selectedYear == MainActivity.this.selectedYear) {
                        // Áp dụng viền cho ngày được chọn (khác với ngày hiện tại)
                        cell.setBackgroundResource(R.drawable.selected_day_border);
                    }

                    // Hiển thị ngày trong tháng
                    cell.setOnClickListener(new View.OnClickListener() {
                        long lastClickTime = 0;
                        @Override
                        public void onClick(View v) {
                            long currentClickTime = System.currentTimeMillis();

                            // Kiểm tra double click trên cùng một ngày
                            if (currentClickTime - lastClickTime < 200 &&
                                    selectedYear == lastClickedYear &&
                                    selectedMonth == lastClickedMonth &&
                                    selectedDay == lastClickedDay) {
                                showNoteDialog(selectedYear, selectedMonth, selectedDay);
                                lastClickTime = 0;
                            } else {
                                // Lưu lại thời gian và ngày nhấp
                                lastClickTime = currentClickTime;
                                lastClickedYear = selectedYear;
                                lastClickedMonth = selectedMonth;
                                lastClickedDay = selectedDay;
                            }

                            MainActivity.this.selectedDay = selectedDay;
                            MainActivity.this.selectedMonth = selectedMonth;
                            MainActivity.this.selectedYear = selectedYear;
                            updateDateInfo(selectedYear, selectedMonth, selectedDay);

                            // Làm mới bảng lịch nhưng không recreate các cell
                            updateCalendarHighlight();
                        }
                    });

                    cell.setText(String.valueOf(date));
                    date++;
                } else {
                    cell.setText("");
                }

                row.addView(cell);
                position++;
            }

            calendarTable.addView(row);
        }
    }


}