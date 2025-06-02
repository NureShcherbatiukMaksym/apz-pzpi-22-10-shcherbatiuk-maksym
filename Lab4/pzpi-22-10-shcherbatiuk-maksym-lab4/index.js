// index.js (серверний файл бекенду)
const { app, server } = require('./app');
const sequelize = require('./config/database');
const { initializeSocket } = require('./services/socketServer');

// Визначаємо порт, який буде слухати наш додаток
// PM2 буде передавати сюди унікальний порт для кожного екземпляра
const PORT = process.env.PORT || 5000; // Це вже у вас є, чудово!

// 1. Спочатку синхронізуємо базу даних
sequelize.sync().then(() => {
    // 2. Запускаємо HTTP-сервер
    server.listen(PORT, () => {
        console.log(`Server is running on port ${PORT}`);
        console.log(`Server is running on port ${PORT}`);
        // 3. Після запуску сервера ініціалізуємо Socket.IO
        (async () => {
            const ioInstance = await initializeSocket(server);
            // 4. Додаємо Socket.IO інстанс до об'єкта Express app
            app.set('socketio', ioInstance);
            console.log(`Socket.IO initialized and attached to app on port ${PORT}.`); // Додамо порт до логу
        })();

    });
}).catch((err) => {
    console.error('Unable to connect to the database:', err);
});