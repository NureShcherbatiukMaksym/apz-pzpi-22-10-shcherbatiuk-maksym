// utils/api.js
import axios from 'axios';

const api = axios.create({
    baseURL: 'http://localhost:5000/api',
    withCredentials: true,
});

let onUnauthorizedCallback = null;


export const setUnauthorizedCallback = (callback) => {
    onUnauthorizedCallback = callback;
};


api.interceptors.response.use(
    response => response,
    async error => {

        if (error.response?.status === 401) {
            console.error('API Interceptor: Received 401 Unauthorized. Calling logout callback.');
            if (onUnauthorizedCallback) {
                try {

                    await onUnauthorizedCallback();
                } catch (logoutErr) {
                    console.error('API Interceptor: Error during unauthorized callback execution:', logoutErr);
                }
            } else {
                console.warn('API Interceptor: 401 received, but no unauthorized callback is set.');
            }
        }


        return Promise.reject(error);
    }
);

export default api;