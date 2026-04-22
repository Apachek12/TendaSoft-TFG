import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { Printer, Settings, BarChart2 } from 'lucide-react';
import ConfiguracionNegocio from './ConfiguracionNegocio';

export default function Topbar({ rol }) {
    const navigate = useNavigate();

    // --- ESTADO PARA EL MODAL DE CONFIGURACIÓN ---
    const [isModalOpen, setIsModalOpen] = useState(false);

    return (
        <div className="h-16 bg-white border-b border-gray-200 flex justify-between items-center px-6 shrink-0 z-40">

            {/* Logo pequeño */}
            <div className="flex items-center">
                <img src="/src/logo-tendasoft.png" alt="Logo" className="w-8 h-8 object-contain" />
            </div>

            {/* Badge de VeriFactu */}
            <div className="bg-[#E0F2F1] border border-[#B2DFDB] text-[#00796B] px-4 py-1.5 rounded-full text-[10px] font-black uppercase tracking-widest flex items-center shadow-sm">
                <div className="w-2 h-2 bg-[#2ECC71] rounded-full mr-2 animate-pulse"></div>
                Sistema VeriFactu Activo
            </div>

            {/* Iconos de la derecha según el rol */}
            <div className="flex items-center space-x-5 text-gray-400">
                {rol === 'ADMIN' && (
                    <>
                        {/* Icono de Reportes/Facturación */}
                        <BarChart2
                            className="w-6 h-6 cursor-pointer hover:text-[#001D3D] transition-colors"
                            onClick={() => navigate('/facturacion')}
                            title="Reportes y Facturación"
                        />

                        {/* Icono de Configuración - ABRE EL MODAL */}
                        <Settings
                            className="w-6 h-6 cursor-pointer hover:text-[#001D3D] transition-colors"
                            onClick={() => setIsModalOpen(true)}
                            title="Configuración de Negocio"
                        />
                    </>
                )}

                {/* Icono de Impresora (Común para todos) */}
                <Printer
                    className="w-6 h-6 cursor-pointer hover:text-[#001D3D] transition-colors"
                    title="Imprimir último ticket"
                />
            </div>

            {/* --- COMPONENTE MODAL DE CONFIGURACIÓN --- */}
            {/* Solo se renderiza si el rol es ADMIN, aunque el estado lo controle */}
            {rol === 'ADMIN' && (
                <ConfiguracionNegocio
                    isOpen={isModalOpen}
                    onClose={() => setIsModalOpen(false)}
                />
            )}
        </div>
    );
}