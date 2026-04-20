import React from 'react';
import { Printer, Settings, BarChart2 } from 'lucide-react';

export default function Topbar({ rol }) {
  return (
    <div className="h-16 bg-white border-b border-gray-200 flex justify-between items-center px-6">
      {/* Logo pequeño */}
      <div className="flex items-center">
        <img src="/src/logo-tendasoft.png" alt="Logo" className="w-8 h-8" />
      </div>

      <div className="bg-teal-700 text-white px-4 py-1 rounded-full text-xs font-bold flex items-center shadow-inner">
        <div className="w-2 h-2 bg-green-400 rounded-full mr-2 animate-pulse"></div>
        VeriFactu Activo
      </div>

      {/* Iconos de la derecha según el rol */}
      <div className="flex space-x-4 text-gray-500">
        {rol === 'ADMIN' && (
          <>
            <BarChart2 className="w-6 h-6 cursor-pointer hover:text-gray-800" />
            <Settings className="w-6 h-6 cursor-pointer hover:text-gray-800" />
          </>
        )}
        <Printer className="w-6 h-6 cursor-pointer hover:text-gray-800" />
      </div>
    </div>
  );
}