import React, { useState, useEffect } from 'react';
import axios from 'axios';
import { X, Upload, Check, ChevronRight } from 'lucide-react';

export default function ConfiguracionNegocioModal({ isOpen, onClose }) {
  const [vista, setVista] = useState('fiscal');
  const [loading, setLoading] = useState(false);

  // Estado inicial vinculado a tu Model DatosNegocio
  const [datos, setDatos] = useState({
    nombreEmpresa: '',
    cif: '',
    direccion: '',
    mensajeTicket: '',
    verifactuActivado: false,
    rutaLogo: '',
    rutaCertificado: ''
  });

  // 1. Cargar datos desde tu GET /api/configuracion
  useEffect(() => {
    if (isOpen) {
      axios.get('http://localhost:8080/api/configuracion')
        .then(res => {
          if (res.data) setDatos(res.data);
        })
        .catch(err => console.log("Configuración no encontrada, se creará una nueva."));
    }
  }, [isOpen]);

  // 2. Guardar datos usando tu POST /api/configuracion
  const handleGuardar = async () => {
    setLoading(true);
    try {
      // Usamos POST porque tu controlador ya gestiona el ID 1 automáticamente
      await axios.post('http://localhost:8080/api/configuracion', datos);
      alert("Configuración guardada con éxito");

      // Si estamos en la primera pantalla, cerramos. Si no, nos quedamos en el diseño.
      if (vista === 'fiscal') onClose();
    } catch (error) {
      console.error(error);
      alert("Error al guardar la configuración.");
    } finally {
      setLoading(false);
    }
  };

  if (!isOpen) return null;

  return (
    <div className="fixed inset-0 bg-[#001D3D]/40 backdrop-blur-sm flex items-center justify-center z-50 p-4">
      <div className="bg-white rounded-[24px] w-full max-w-2xl shadow-2xl overflow-hidden border border-gray-100 animate-in zoom-in-95 duration-200">

        {/* Cabecera idéntica a la captura */}
        <div className="p-6 flex justify-between items-center bg-white border-b border-gray-50">
          <h2 className="text-[#001D3D] text-2xl font-black tracking-tight">
            {vista === 'fiscal' ? 'Datos fiscales y AEAT' : 'Configuración del ticket'}
          </h2>
          <button onClick={onClose} className="p-2 hover:bg-gray-100 rounded-full transition-colors">
            <X size={24} className="text-[#001D3D]" />
          </button>
        </div>

        <div className="p-8">
          {vista === 'fiscal' ? (
            /* --- VISTA DATOS FISCALES --- */
            <div className="space-y-4">
              <div className="space-y-1">
                <label className="text-[10px] font-black text-[#95A5A6] uppercase ml-2">Razón Social</label>
                <input
                  placeholder="Razón social (nombre legal)"
                  className="w-full h-14 px-5 bg-[#F8F9FA] rounded-2xl border-2 border-transparent focus:border-[#00796B] outline-none font-bold text-[#2C3E50] transition-all"
                  value={datos.nombreEmpresa}
                  onChange={e => setDatos({...datos, nombreEmpresa: e.target.value})}
                />
              </div>

              <div className="grid grid-cols-2 gap-4">
                <div className="space-y-1">
                  <label className="text-[10px] font-black text-[#95A5A6] uppercase ml-2">NIF / CIF</label>
                  <input
                    placeholder="NIF / CIF"
                    className="w-full h-14 px-5 bg-[#F8F9FA] rounded-2xl border-2 border-transparent focus:border-[#00796B] outline-none font-bold text-[#2C3E50]"
                    value={datos.cif}
                    onChange={e => setDatos({...datos, cif: e.target.value})}
                  />
                </div>
                <div className="space-y-1">
                  <label className="text-[10px] font-black text-[#95A5A6] uppercase ml-2">Dirección</label>
                  <input
                    placeholder="Dirección fiscal"
                    className="w-full h-14 px-5 bg-[#F8F9FA] rounded-2xl border-2 border-transparent focus:border-[#00796B] outline-none font-bold text-[#2C3E50]"
                    value={datos.direccion}
                    onChange={e => setDatos({...datos, direccion: e.target.value})}
                  />
                </div>
              </div>

              <div className="flex items-center justify-between p-4 bg-[#F8F9FA] rounded-2xl my-6">
                <div>
                  <p className="font-bold text-[#2C3E50]">Activar envío automático a Hacienda</p>
                  <p className="text-xs text-[#95A5A6]">Cumplimiento con sistema VeriFactu</p>
                </div>
                <button
                  onClick={() => setDatos({...datos, verifactuActivado: !datos.verifactuActivado})}
                  className={`w-14 h-8 rounded-full transition-all relative ${datos.verifactuActivado ? 'bg-[#001D3D]' : 'bg-gray-300'}`}
                >
                  <div className={`absolute top-1 bg-white w-6 h-6 rounded-full shadow-sm transition-all ${datos.verifactuActivado ? 'left-7' : 'left-1'}`} />
                </button>
              </div>

              <button className="w-full h-14 border-2 border-dashed border-[#E0E6ED] rounded-2xl text-[#34495E] font-black text-xs flex items-center justify-center gap-3 hover:bg-gray-50 uppercase tracking-widest transition-all">
                <Upload size={18} /> Cargar Certificado Digital (.p12)
              </button>

              <button
                onClick={() => setVista('ticket')}
                className="w-full h-14 bg-gray-100 text-[#001D3D] font-black rounded-2xl hover:bg-gray-200 transition-all uppercase text-xs tracking-widest flex items-center justify-center"
              >
                Configurar Ticket <ChevronRight size={18} className="ml-2" />
              </button>
            </div>
          ) : (
            /* --- VISTA DISEÑO TICKET --- */
            <div className="flex gap-8 animate-in slide-in-from-right-4 duration-300">
              <div className="flex-1 space-y-4">
                <div className="space-y-1">
                  <label className="text-[10px] font-black text-[#95A5A6] uppercase ml-2">Nombre Comercial</label>
                  <input
                    className="w-full h-14 px-5 bg-[#F8F9FA] rounded-2xl outline-none font-bold text-[#2C3E50]"
                    value={datos.nombreEmpresa}
                    onChange={e => setDatos({...datos, nombreEmpresa: e.target.value})}
                  />
                </div>
                <div className="space-y-1">
                  <label className="text-[10px] font-black text-[#95A5A6] uppercase ml-2">Pie de página del ticket</label>
                  <textarea
                    className="w-full h-32 p-5 bg-[#F8F9FA] rounded-2xl outline-none font-bold text-[#2C3E50] resize-none"
                    value={datos.mensajeTicket}
                    onChange={e => setDatos({...datos, mensajeTicket: e.target.value})}
                  />
                </div>
                <button className="w-full h-14 border-2 border-dashed border-[#E0E6ED] rounded-2xl text-[#34495E] font-black text-xs flex items-center justify-center gap-3">
                  <Upload size={18} /> CARGAR LOGO
                </button>
                <button onClick={() => setVista('fiscal')} className="text-[#00796B] font-black text-[10px] uppercase tracking-widest hover:underline">
                  Volver a datos fiscales
                </button>
              </div>

              {/* Preview Real del Ticket */}
              <div className="w-64 bg-white border-t-8 border-[#001D3D] p-5 shadow-xl text-[10px] font-mono text-[#2C3E50]">
                <div className="text-center mb-6">
                  <p className="font-black text-xs mb-1 uppercase tracking-tighter">{datos.nombreEmpresa || 'NOMBRE DE LA TIENDA'}</p>
                  <p className="opacity-60">CIF: {datos.cif || '---------'}</p>
                  <p className="opacity-60 uppercase">{datos.direccion || 'DIRECCIÓN'}</p>
                </div>
                <div className="border-t border-dashed border-gray-300 my-3 pt-3">
                  <div className="flex justify-between font-black mb-2 uppercase">
                    <span>CANT. CONCEPTO</span>
                    <span>IMPORTE</span>
                  </div>
                  <div className="flex justify-between opacity-70">
                    <span>1x Producto Demo</span>
                    <span>10,00 €</span>
                  </div>
                </div>
                <div className="border-t-2 border-black my-3 pt-3 flex justify-between font-black text-sm">
                  <span>TOTAL</span>
                  <span>10,00 €</span>
                </div>
                <div className="text-center mt-6 space-y-3 opacity-60">
                  <p className="font-bold italic">"{datos.mensajeTicket || 'Gracias por su visita'}"</p>
                  <div className="w-12 h-12 bg-gray-100 mx-auto flex items-center justify-center text-[8px] border border-gray-200 uppercase">QR</div>
                  <p className="text-[7px] tracking-[4px] font-black">SISTEMA VERIFACTU</p>
                </div>
              </div>
            </div>
          )}

          {/* Botón Guardar Final */}
          <button
            onClick={handleGuardar}
            disabled={loading}
            className="w-full h-16 bg-[#00796B] text-white font-black rounded-2xl mt-10 hover:bg-[#004D40] active:scale-[0.98] transition-all flex items-center justify-center gap-3 shadow-lg shadow-[#00796B]/20"
          >
            {loading ? 'Guardando...' : <><Check size={24} /> GUARDAR CONFIGURACIÓN</>}
          </button>
        </div>
      </div>
    </div>
  );
}