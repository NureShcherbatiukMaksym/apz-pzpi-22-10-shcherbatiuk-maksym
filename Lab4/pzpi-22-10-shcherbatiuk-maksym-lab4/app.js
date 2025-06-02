// app.js (витяг з вашого app.js, переконайтеся, що він включає test-instance та clusterId)
const express = require('express');
const cors = require('cors');
const cookieParser = require('cookie-parser');
const session = require('express-session');
require('dotenv').config();
const http = require('http');
const path = require('path');

const { RedisStore } = require('connect-redis');
const { createClient } = require('redis');

const app = express();
const server = http.createServer(app);

const redisClient = createClient({ url: "redis://localhost:6379" });

redisClient.on('error', (err) => console.error('Redis Session Client Error:', err));
redisClient.connect()
    .then(() => console.log('Redis client for sessions connected successfully.'))
    .catch(err => console.error('Failed to connect to Redis for sessions:', err));

let redisStore;
try {
    redisStore = new RedisStore({
        client: redisClient,
        prefix: "soilscout_sess:",
        session: session,
    });
    console.log('RedisStore initialized successfully.');
} catch (e) {
    console.error('ERROR: Failed to instantiate new RedisStore:', e.message);
    redisStore = new session.MemoryStore();
}

const authRoutes = require('./routes/auth');
const usersRoutes = require('./routes/users');
const fieldRouter = require('./routes/fields');
const measurementPointsRouter = require('./routes/measurementPoints');
const sensorRoutes = require('./routes/sensors');
const measurementRoutes = require('./routes/measurements');
const iotDeviceRouter = require('./routes/iotDevice');
const userIotDeviceRouter = require('./routes/userIotDevice');
const fieldMeasurementsRouter = require('./routes/fieldMeasurement');


app.use(cors({
    origin: 'http://localhost:3000',
    credentials: true
}));

app.use(session({
    store: redisStore,
    secret: process.env.SESSION_SECRET || 'ATzX_U63NBL[S2C74$b-ua',
    resave: false,
    saveUninitialized: true,
    cookie: {
        secure: process.env.NODE_ENV === 'production',
        maxAge: 3600000,
        httpOnly: true
    }
}));

app.use(express.json());
app.use(cookieParser());

// Маршрути
app.use('/api/auth', authRoutes);
app.use('/api/users', usersRoutes);
app.use('/api/fields', fieldRouter);
app.use('/api/measurement-points', measurementPointsRouter);
app.use('/api/sensors', sensorRoutes);
app.use('/api/measurements', measurementRoutes);
app.use('/api/iot-devices', iotDeviceRouter);
app.use('/api/user-iot-devices', userIotDeviceRouter);
app.use('/api/field-measurements', fieldMeasurementsRouter);
app.use('/uploads', express.static(path.join(__dirname, '../uploads')));



// Додаємо маршрут для тестування розподілу навантаження
const clusterId = process.env.NODE_APP_INSTANCE || 'N/A'; // Отримуємо ID кластера PM2
const currentPort = process.env.PORT || 'N/A'; // Отримуємо поточний порт

app.get('/api/test-instance', (req, res) => {
    console.log(`[Instance ${clusterId} on Port ${currentPort}] Handling GET request to /api/test-instance`);
    res.json({ message: `Hello from instance ${clusterId} on port ${currentPort}` });
});

module.exports = { app, server };