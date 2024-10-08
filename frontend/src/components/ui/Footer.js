import React from 'react';
import { Link } from 'react-router-dom';

const Footer = () => (
    <footer className="bg-teal-500 text-white mt-8">
        <div className="container mx-auto px-4 py-8">
            <div className="flex flex-col md:flex-row justify-between items-center">
                <div className="mb-4 md:mb-0">
                    <Link to="/" className="text-2xl font-bold">축제의 민족</Link>
                    <p className="text-sm mt-2">Discover and celebrate amazing festivals</p>
                </div>
                <div className="flex space-x-4">
                    <Link to="/privacy" className="text-sm hover:text-teal-200 transition-colors">Privacy Policy</Link>
                    <Link to="/terms" className="text-sm hover:text-teal-200 transition-colors">Terms of Service</Link>
                </div>
            </div>
            <div className="mt-8 text-center text-sm">
                © {new Date().getFullYear()} 축제의 민족, Inc. All rights reserved.
            </div>
        </div>
    </footer>
);

export default Footer;