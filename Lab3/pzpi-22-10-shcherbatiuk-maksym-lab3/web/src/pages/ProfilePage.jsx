import React from 'react';
import DashboardContent from '../components/DashboardContent';
import Profile from '../components/Profile';

const ProfilePage = () => {
    return (
        <div className="flex flex-wrap">
            <DashboardContent />
            <Profile />
        </div>
    );
}

export default ProfilePage;
