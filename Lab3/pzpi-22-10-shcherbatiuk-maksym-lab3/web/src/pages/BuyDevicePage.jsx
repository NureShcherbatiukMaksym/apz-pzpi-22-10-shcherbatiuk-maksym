import React from 'react';

const devices = [
    {
        id: 1,
        name: 'SoilScout Pro',
        price: '450 $',
        sensors: ['Температура', 'Вологість', 'Кислотність'],
        description: 'Ідеальне рішення для моніторингу невеликих полів та теплиць.',
        image: '/images/device1.png',
    },
    {
        id: 2,
        name: 'SoilScout Ultra',
        price: '750 $',
        sensors: ['Температура', 'Вологість', 'Кислотність', 'Солоність'],
        description: 'Професійна модель для великих аграрних підприємств.',
        image: '/images/device2.png',
    },
];

const BuyDevicePage = () => {
    return (
        <div className="p-6 space-y-8">
            <h1 className="text-4xl font-bold text-center mb-8">Купівля пристроїв</h1>

            <div className="grid md:grid-cols-2 gap-8">
                {devices.map(device => (
                    <div key={device.id} className="border rounded-2xl shadow-lg p-6 flex flex-col items-center text-center">
                        <img src={device.image} alt={device.name} className="w-40 h-40 object-contain mb-4" />
                        <h2 className="text-2xl font-semibold">{device.name}</h2>
                        <p className="text-primary text-xl mt-2">{device.price}</p>
                        <ul className="mt-4 space-y-2 text-sm">
                            {device.sensors.map((sensor, idx) => (
                                <li key={idx}>🔹 {sensor}</li>
                            ))}
                        </ul>
                        <p className="mt-4 text-gray-600">{device.description}</p>
                    </div>
                ))}
            </div>
        </div>
    );
};

export default BuyDevicePage;
