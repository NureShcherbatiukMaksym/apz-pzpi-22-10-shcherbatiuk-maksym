// Dashboard.jsx
import { FieldProvider } from '../contexts/FieldContext';
import Sidebar from './Sidebar';
import Navbar from './Navbar';
import { Outlet } from 'react-router-dom';

export default function Dashboard() {
    return (
        <FieldProvider>
            <div className="flex h-screen bg-gray-100 font-sans">
                <Sidebar />
                <div className="flex flex-col flex-1 pl-16">
                    <Navbar />
                    <div>
                        <Outlet />
                    </div>
                </div>
            </div>
        </FieldProvider>
    );
}
