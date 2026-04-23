import React, { useState, useEffect, useCallback } from 'react';
import { useNavigate } from 'react-router-dom';
import axios from 'axios';
import { Printer, Settings, BarChart2, AlertCircle } from 'lucide-react';
import ConfiguracionNegocioModal from './ConfiguracionNegocio';
import ConfiguracionImpresoraModal from './AñadirImpresora';

export default function Topbar({ rol }) {
    const navigate = useNavigate();

    const [isModalOpen, setIsModalOpen] = useState(false);
    const [isModalImpresoraOpen, setIsImpresoraModalOpen] = useState(false);
    const [verifactuActivo, setVerifactuActivo] = useState(false);
    const [cargandoEstado, setCargandoEstado] = useState(true);

    // 1. EXTRAEMOS LA FUNCIÓN PARA PODER LLAMARLA CUANDO QUERAMOS
    const comprobarVerifactu = useCallback(async () => {
        setCargandoEstado(true);
        try {
            const response = await axios.get('http://localhost:8080/api/configuracion');
            // Recuerda usar verifactuActivado (como vimos en el JSON)
            setVerifactuActivo(response.data.verifactuActivado === true);
        } catch (error) {
            console.error("Error al comprobar el estado de VeriFactu", error);
            setVerifactuActivo(false);
        } finally {
            setCargandoEstado(false);
        }
    }, []);

    // 2. EL USEEFFECT AHORA SOLO LLAMA A LA FUNCIÓN AL CARGAR LA PÁGINA
    useEffect(() => {
        comprobarVerifactu();
    }, [comprobarVerifactu]);

    return (
        <div className="h-16 bg-white border-b border-gray-200 flex justify-between items-center px-6 shrink-0 z-40">
            <div className="flex items-center">
                <img src="/src/logo-tendasoft.png" alt="Logo" className="w-8 h-8 object-contain" />
            </div>

            {!cargandoEstado && (
                <div className={`px-4 py-1.5 rounded-full text-[10px] font-black uppercase tracking-widest flex items-center shadow-sm transition-colors ${
                    verifactuActivo
                    ? 'bg-[#E0F2F1] border border-[#B2DFDB] text-[#00796B]'
                    : 'bg-slate-100 border border-slate-200 text-slate-500'
                }`}>
                    {verifactuActivo ? (
                        <div className="w-2 h-2 bg-[#2ECC71] rounded-full mr-2 animate-pulse"></div>
                    ) : (
                        <AlertCircle size={12} className="mr-1.5 text-slate-400" />
                    )}
                    {verifactuActivo ? 'Sistema VeriFactu Activo' : 'Sistema VeriFactu Inactivo'}
                </div>
            )}

            <div className="flex items-center space-x-5 text-gray-400">
                {rol === 'ADMIN' && (
                    <>
                        <BarChart2
                            className="w-6 h-6 cursor-pointer hover:text-[#001D3D] transition-colors"
                            onClick={() => navigate('/facturacion')}
                        />
                        <Settings
                            className="w-6 h-6 cursor-pointer hover:text-[#001D3D] transition-colors"
                            onClick={() => setIsModalOpen(true)}
                        />
                    </>
                )}
                <Printer
                    className="w-6 h-6 cursor-pointer hover:text-[#001D3D] transition-colors"
                    onClick={() => setIsImpresoraModalOpen(true)}/>
            </div>

            {/* 3. LE PASAMOS LA FUNCIÓN AL MODAL COMO UNA "PROP" */}
            {rol === 'ADMIN' && (
                <ConfiguracionNegocioModal
                    isOpen={isModalOpen}
                    onClose={() => setIsModalOpen(false)}
                    onConfiguracionGuardada={comprobarVerifactu} // <--- ESTO ES LA MAGIA
                />
            )}

            {(<ConfiguracionImpresoraModal
                isOpen={isModalImpresoraOpen}
                onClose={() => setIsImpresoraModalOpen(false)}
                />
            )}
        </div>
    );
}