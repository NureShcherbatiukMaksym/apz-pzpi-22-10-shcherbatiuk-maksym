import React from 'react';

const AboutPage = () => {
    return (
        <div className="p-6 space-y-12">

            <section className="text-center">
                <h1 className="text-4xl font-bold mb-4">Про SoilScout</h1>
                <p className="text-lg max-w-2xl mx-auto">
                    Ми розробляємо рішення для моніторингу стану ґрунту, що допомагають аграріям отримувати точні дані в реальному часі.
                </p>
                <img src="/images/about-us.jpg" alt="Про нас" className="mt-6 rounded-2xl mx-auto shadow-lg" />
            </section>

            <section className="text-center">
                <h2 className="text-3xl font-semibold mb-4">Наша технологія</h2>
                <p className="text-lg max-w-2xl mx-auto">
                    Інноваційні датчики з високою точністю вимірюють температуру, вологість і кислотність ґрунту.
                </p>
                <img src="/images/technology-diagram.png" alt="Технологія" className="mt-6 rounded-2xl mx-auto shadow-lg" />
            </section>

            <section className="text-center">
                <h2 className="text-3xl font-semibold mb-4">Платформи</h2>
                <div className="flex justify-center gap-8 flex-wrap">
                    <div className="w-60">
                        <img src="/images/web-platform.png" alt="Веб-платформа" className="rounded-xl shadow-md" />
                        <p className="mt-2 font-medium">Веб-платформа</p>
                    </div>
                    <div className="w-60">
                        <img src="/images/mobile-app.png" alt="Мобільний застосунок" className="rounded-xl shadow-md" />
                        <p className="mt-2 font-medium">Мобільний застосунок</p>
                    </div>
                </div>
            </section>
        </div>
    );
};

export default AboutPage;
