import React from 'react';

const Header = () => {
    return (
        <nav id="header" className="bg-gray-200 w-full lg:max-w-sm flex items-center border-b-1 border-gray-300 order-2 lg:order-1">
            <div className="px-2 w-full">
                <select name="" className="bg-gray-300 border-2 border-gray-200 rounded-full w-full py-3 px-4 text-gray-500 font-bold leading-tight focus:outline-none focus:bg-white focus:shadow-md" id="form-field2">
                    <option value="Default">default</option>
                    <option value="A">report a</option>
                    <option value="B">report b</option>
                    <option value="C">report c</option>
                </select>
            </div>
        </nav>
    );
}

export default Header;
