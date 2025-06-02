import { Link, useLocation } from 'react-router-dom';
import { useContext } from 'react';
import { UserContext } from '../contexts/UserContext';
import {useTranslation} from "react-i18next";

export default function Sidebar() {
    const { isAdmin } = useContext(UserContext);
    const location = useLocation();
    const { t } = useTranslation();

    return (

        <div id="sidebar" className="h-screen w-16 menu bg-white px-4 flex items-center nunito static fixed shadow">
            <ul className="list-reset">
                {[
                    { icon: 'fa-home', text: t(`home_page`), path: '/home' },
                    { icon: 'fa-chart-area', text: t(`analytics_page`), path: '/analytics' },
                    { icon: 'fa-envelope', text: t(`messages`), path: '/messages' },

                    isAdmin && { icon: 'fa-tools', text: t(`admin_panel`), path: '/admin' }
                ]

                    .filter(item => item)
                    .map(({ icon, text, path }, index) => {

                        const isActive = location.pathname.startsWith(path);

                        return (
                            <li key={index} className="my-2 md:my-0">
                                <Link
                                    to={path}
                                    className={`block py-1 md:py-3 pl-1 align-middle no-underline
                                        ${isActive ? 'text-indigo-400 font-bold' : 'text-gray-600'}
                                        hover:text-indigo-400`}
                                >
                                    <i className={`fas ${icon} fa-fw mr-3`}></i>

                                    <span className="w-full inline-block pb-1 md:pb-0 text-sm whitespace-nowrap overflow-hidden text-ellipsis">
                                        {text}
                                    </span>
                                </Link>
                            </li>
                        );
                    })}
            </ul>
        </div>
    );
}