import React, { useContext } from 'react';
import { BrowserRouter as Router, Routes, Route, Navigate } from 'react-router-dom';
import { LoadScript } from "@react-google-maps/api";
import { UserProvider, UserContext } from './contexts/UserContext';
import ProtectedRoute from './utils/PrivateRoute';
import Dashboard from './components/Dashboard';
import HomePage from './pages/HomePage';
import AnalyticsPage from './pages/AnalyticsPage';
import AnalyticsFieldPage from "./pages/AnalyticsFieldPage";
import LoginPage from './pages/LoginPage';
import RegisterPage from './pages/RegisterPage';
import CreateFieldPage from './pages/CreateFieldPage';
import FieldPage from './pages/FieldPage';
import ProfilePage from './pages/ProfilePage';
import AdminPage from './pages/AdminPage';
import AboutPage from "./pages/AboutPage";
import ContactPage from './pages/ContactPage';
import BuyDevicePage from "./pages/BuyDevicePage";

import './utils/i18n';
import "./styles/App.css";
import './styles/index.css';


const Maps_API_KEY = "AIzaSyC6-KT4EaMNrVuln5DQ5Xn8e8n4kZwsKMU";


function App() {
    return (
        <LoadScript googleMapsApiKey={Maps_API_KEY} libraries={['geometry']}>
            <Router>
                <UserProvider>
                    <Routes>
                        {/* Публічні сторінки */}
                        <Route path="/login" element={<LoginPage />} />
                        <Route path="/register" element={<RegisterPage />} />
                        <Route path='/about' element={<AboutPage/>} />
                        <Route path='/contact' element={<ContactPage/>} />
                        <Route path='/buy-device' element={<BuyDevicePage/>} />

                        {/* Захищені маршрути - рендеряться всередині лейауту Dashboard */}
                        <Route
                            path="/"
                            element={
                                <ProtectedRoute>
                                    {/* Dashboard рендерить DashboardContent та <Outlet /> */}
                                    <Dashboard />
                                </ProtectedRoute>
                            }
                        >
                            {/* Вкладені маршрути - рендеряться в <Outlet /> */}
                            <Route index element={<Navigate to="home" replace />} />
                            <Route path="home" element={<HomePage />} />
                            <Route path="create-field" element={<CreateFieldPage />} />
                            <Route path="field/:id/edit" element={<CreateFieldPage />} />
                            <Route path="home/field/:id" element={<FieldPage />} />

                            {/* Маршрути аналітики */}
                            <Route path="analytics" element={<AnalyticsPage />} />
                            <Route path="analytics/field/:id" element={<AnalyticsFieldPage />} />


                            <Route path="profile" element={<ProfilePage />} />
                            <Route path="admin" element={<AdminPage />} />

                        </Route>

                        {/* Маршрут для будь-яких інших не співпадаючих шляхів - перенаправляє на головну */}
                        <Route path="*" element={<Navigate to="/" replace />} />

                    </Routes>
                </UserProvider>
            </Router>
        </LoadScript>
    );
}

export default App;