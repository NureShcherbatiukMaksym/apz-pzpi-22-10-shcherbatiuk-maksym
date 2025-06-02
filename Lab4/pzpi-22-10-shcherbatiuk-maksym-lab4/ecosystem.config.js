module.exports = {
    apps : [{
        name: 'my-backend',
        script: 'index.js',
        instances: "2", // Або фіксоване число, наприклад, 8
        autorestart: true,
        watch: false,
        max_memory_restart: '1G',
        env: {
            NODE_ENV: 'development',
            PORT: 5001 // *** ПОЧАТКОВИЙ ПОРТ ДЛЯ ПЕРШОГО ЕКЗЕМПЛЯРА ***
        },
        env_production: {
            NODE_ENV: 'production',
            PORT: 5001 // *** ПОЧАТКОВИЙ ПОРТ ДЛЯ ПЕРШОГО ЕКЗЕМПЛЯРА В PROD ***
        },
        exec_mode: 'cluster',
        increment_var: 'PORT' // PM2 буде інкрементувати змінну PORT, починаючи з значення, вказаного в 'env.PORT'
    }]
};