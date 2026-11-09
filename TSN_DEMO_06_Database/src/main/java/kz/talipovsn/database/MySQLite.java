package kz.talipovsn.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

public class MySQLite extends SQLiteOpenHelper {

    private Context context;

    // Конструктор класса
    public MySQLite(Context context) {
        // Увеличили версию до 14, чтобы таблица пересоздалась
        super(context, "phones", null, 14);
        this.context = context;
    }

    // Создание таблицы
    @Override
    public void onCreate(SQLiteDatabase db) {
        // Создание таблицы emergency_service с тремя колонками: id, name, phone_number
        db.execSQL("CREATE TABLE emergency_service (" +
                "id INTEGER PRIMARY KEY, " +
                "name TEXT, " +
                "phone_number TEXT)");

        loadDataFromAsset(db); // Чтение данных из файла assets/telephone.txt и формирование базы данных
    }

    // Пересоздание таблицы при смене версии БД или при удалении таблицы
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS emergency_service"); // Удаление таблицы, если она существует
        onCreate(db); // Вызов метода onCreate() для пересоздания таблицы
    }

    // Добавление одной записи в таблицу через метод insert() класса SQLiteDatabase
    public void addData(SQLiteDatabase db, String name, String phone) {
        ContentValues values = new ContentValues(); // Создание объекта ContentValues для хранения данных
        values.put("name", name); // Добавление имени в объект ContentValues
        values.put("phone_number", phone); // Добавление номера телефона в объект ContentValues
        db.insert("emergency_service", null, values); // Вставка данных в таблицу emergency_service
    }

    // Чтение данных из файла assets/telephone.txt и формирование базы данных
    public void loadDataFromAsset(SQLiteDatabase db) {
        try (InputStream is = context.getAssets().open("telephone.txt");
             BufferedReader reader = new BufferedReader(new InputStreamReader(is))) {

            String line; // Чтение каждой строки из файла
            while ((line = reader.readLine()) != null) { // Чтение строки из файла
                if (!line.trim().isEmpty()) { // Проверка на пустую строку
                    String[] parts = line.split("\\|"); // Разделение строки на части по символу "|"
                    String name = parts[0].trim(); // Убираем пробелы в начале и конце имени
                    String phone = parts[1].trim(); // Убираем пробелы в начале и конце номера телефона
                    addData(db, name, phone); // Добавление данных в базу данных
                }
            }
        } catch (Exception ignored) {
        }
    }

    // Получение данных из базы данных с учетом фильтра
    public String getData(String filter) {
        SQLiteDatabase db = this.getReadableDatabase(); // Получаем базу данных для чтения

        // Делаем один простой запрос к базе данных, чтобы получить все записи, отсортированные по имени
        Cursor cursor = db.rawQuery("SELECT * FROM emergency_service ORDER BY name", null);

        StringBuilder result = new StringBuilder();
        String filterLc = filter.toLowerCase(); // Переводим фильтр в нижний регистр один раз
        int num = 0;

        // Проходим по всем записям в курсоре
        while (cursor.moveToNext()) {
            String name = cursor.getString(1);  // 1 — колонка "name"
            String phone = cursor.getString(2); // 2 — колонка "phone_number"

            // Проверяем совпадение с фильтром (по имени или по номеру телефона)
            if (name.toLowerCase().contains(filterLc) || phone.contains(filter)) {
                num++;
                result.append(num).append(") ").append(name).append(": ").append(phone).append("\n");
            }
        }

        cursor.close(); // Закрываем курсор после использования
        return result.toString(); // Возвращаем результат в виде строки
    }
}