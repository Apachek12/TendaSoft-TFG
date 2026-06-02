import React, { useState, useEffect } from 'react';
import axios from 'axios';
import {
  X, Upload, Check, ChevronRight, CheckCircle2, XCircle, Loader2, Lock
} from 'lucide-react';
import { toast } from 'react-hot-toast';

const API = import.meta.env.VITE_API_URL;

export default function ConfiguracionNegocioModal({ isOpen, onClose, onConfiguracionGuardada }) {
  const [vista, setVista]                   = useState('fiscal');
  const [paso, setPaso]                     = useState('formulario');
  const [loading, setLoading]               = useState(false);
  const [logoFile, setLogoFile]             = useState(null);
  const [logoPreview, setLogoPreview]       = useState(null);
  const [certificadoFile, setCertificadoFile] = useState(null);
  const [datos, setDatos]                   = useState({
    nombreEmpresa:        '',
    cif:                  '',
    direccion:            '',
    mensajeTicket:        '',
    verifactuActivado:    false,
    rutaLogo:             '',
    rutaCertificado:      '',
    certificadoPassword:  ''
  });

  useEffect(() => {
    if (!isOpen) return;
    setPaso('formulario');

    axios.get(`${API}/api/configuracion`)
      .then(({ data }) => {
        if (data) {
          setDatos({
            ...data,
            // Placeholder visual de contraseña si VeriFactu ya estaba activado
            certificadoPassword: data.verifactuActivado ? '********' : ''
          });
        }
      })
      .catch(() => {}); // Primera vez sin configuración: ignorar
  }, [isOpen]);

  // Requisitos Verifactu
  const tieneNif          = !!datos.cif?.trim();
  const tieneCertificado  = !!(datos.rutaCertificado?.trim() || certificadoFile);
  const tienePassword     = !!datos.certificadoPassword?.trim();
  const puedeActivarVerifactu = tieneNif && tieneCertificado && tienePassword;

  const handleToggleVerifactu = () => {
    if (!datos.verifactuActivado && !puedeActivarVerifactu) {
      toast.error('Faltan requisitos: NIF, Certificado y Contraseña.');
      return;
    }
    setDatos({ ...datos, verifactuActivado: !datos.verifactuActivado });
  };

  const handleLogoChange = (e) => {
    const file = e.target.files[0];
    if (file) { setLogoFile(file); setLogoPreview(URL.createObjectURL(file)); }
  };

  const handleCertificadoChange = (e) => {
    const file = e.target.files[0];
    if (file) setCertificadoFile(file);
  };

  const handleGuardar = async () => {
    setLoading(true);
    try {
      const formData = new FormData();
      Object.entries({
        nombreEmpresa:       datos.nombreEmpresa,
        cif:                 datos.cif,
        direccion:           datos.direccion,
        mensajeTicket:       datos.mensajeTicket,
        verifactuActivado:   datos.verifactuActivado,
        certificadoPassword: datos.certificadoPassword
      }).forEach(([k, v]) => formData.append(k, v));

      if (logoFile)        formData.append('logo', logoFile);
      if (certificadoFile) formData.append('certificado', certificadoFile);

      await axios.post(`${API}/api/configuracion`, formData);
      onConfiguracionGuardada?.();
      setPaso('exito');
    } catch {
      toast.error('Error al guardar la configuración.');
    } finally {
      setLoading(false);
    }
  };

  const resetModal = () => { setPaso('formulario'); setVista('fiscal'); onClose(); };

  if (!isOpen) return null;

  return (
    <div className="fixed inset-0 bg-[#001D3D]/40 backdrop-blur-sm flex items-center justify-center z-50 p-4">
      <div className="bg-white rounded-[24px] w-full max-w-2xl shadow-2xl overflow-hidden border border-gray-100 animate-in zoom-in-95 duration-200">

        {paso === 'formulario' && (
          <div className="p-6 flex justify-between items-center border-b border-gray-50">
            <h2 className="text-[#001D3D] text-2xl font-black tracking-tight">
              {vista === 'fiscal' ? 'Configuración Fiscal y AEAT' : 'Personalización Ticket'}
            </h2>
            <button onClick={onClose} className="p-2 hover:bg-gray-100 rounded-full transition-colors">
              <X size={24} />
            </button>
          </div>
        )}

        <div className="p-8">
          {paso === 'formulario' ? (
            <>
              {vista === 'fiscal' ? (
                <div className="space-y-4 animate-in slide-in-from-left-4 duration-300">
                  <div className="space-y-1">
                    <label className="text-[10px] font-black text-[#95A5A6] uppercase ml-2">Razón Social</label>
                    <input
                      className="w-full h-14 px-5 bg-[#F8F9FA] rounded-2xl border-2 border-transparent focus:border-[#00796B] outline-none font-bold"
                      value={datos.nombreEmpresa}
                      onChange={e => setDatos({ ...datos, nombreEmpresa: e.target.value })}
                    />
                  </div>

                  <div className="grid grid-cols-2 gap-4">
                    <input
                      placeholder="NIF / CIF"
                      className="w-full h-14 px-5 bg-[#F8F9FA] rounded-2xl outline-none font-bold"
                      value={datos.cif}
                      onChange={e => setDatos({ ...datos, cif: e.target.value })}
                    />
                    <input
                      placeholder="Dirección fiscal"
                      className="w-full h-14 px-5 bg-[#F8F9FA] rounded-2xl outline-none font-bold"
                      value={datos.direccion}
                      onChange={e => setDatos({ ...datos, direccion: e.target.value })}
                    />
                  </div>
                  <div className="p-5 bg-slate-50 border border-slate-200 rounded-2xl my-6">
                    <div className="flex items-center justify-between mb-4">
                      <div>
                        <p className="font-bold text-[#2C3E50]">Integración VeriFactu</p>
                        <p className="text-xs text-[#95A5A6]">Estado de cumplimiento legal</p>
                      </div>
                      <button
                        onClick={handleToggleVerifactu}
                        className={`w-14 h-8 rounded-full transition-all relative ${datos.verifactuActivado ? 'bg-[#00796B]' : 'bg-gray-300'}`}
                      >
                        <div className={`absolute top-1 bg-white w-6 h-6 rounded-full shadow-sm transition-all ${datos.verifactuActivado ? 'left-7' : 'left-1'}`} />
                      </button>
                    </div>
                    <div className="space-y-2 pt-3 border-t">
                      {[
                        { label: 'NIF Validado', ok: tieneNif         },
                        { label: 'Archivo .p12', ok: tieneCertificado },
                        { label: 'Contraseña',   ok: tienePassword    }
                      ].map(({ label, ok }) => (
                        <div key={label} className="flex justify-between text-xs font-bold uppercase">
                          <span>{label}</span>
                          {ok
                            ? <CheckCircle2 size={14} className="text-green-500" />
                            : <XCircle size={14} className="text-red-400" />
                          }
                        </div>
                      ))}
                    </div>
                  </div>
                  <div className="grid grid-cols-2 gap-4">
                    <label className="h-14 border-2 border-dashed border-[#E0E6ED] rounded-2xl text-[#34495E] font-black text-[10px] flex items-center justify-center gap-2 hover:bg-gray-50 cursor-pointer uppercase transition-colors">
                      <Upload size={16} /> {certificadoFile ? 'CAMBIAR .P12' : 'SUBIR CERTIFICADO'}
                      <input type="file" accept=".p12,.pfx" className="hidden" onChange={handleCertificadoChange} />
                    </label>
                    <div className="relative">
                      <Lock className="absolute left-4 top-4 text-slate-300" size={18} />
                      <input
                        type="password"
                        placeholder="Contraseña del certificado"
                        className="w-full h-14 pl-12 pr-4 bg-[#F8F9FA] rounded-2xl border-2 border-transparent focus:border-[#00796B] outline-none font-bold text-sm"
                        value={datos.certificadoPassword}
                        onChange={e => setDatos({ ...datos, certificadoPassword: e.target.value })}
                      />
                    </div>
                  </div>
                  <button
                    onClick={() => setVista('ticket')}
                    className="w-full h-14 bg-gray-100 text-[#001D3D] font-black rounded-2xl hover:bg-gray-200 transition-all uppercase text-xs tracking-widest flex items-center justify-center mt-2"
                  >
                    Diseño del Ticket <ChevronRight size={18} className="ml-2" />
                  </button>
                </div>
              ) : (
                <div className="flex gap-8 animate-in slide-in-from-right-4 duration-300">
                  <div className="flex-1 space-y-4">
                    <div className="space-y-1">
                      <label className="text-[10px] font-black text-[#95A5A6] uppercase ml-2">Nombre Comercial</label>
                      <input
                        className="w-full h-14 px-5 bg-[#F8F9FA] rounded-2xl outline-none font-bold text-[#2C3E50]"
                        value={datos.nombreEmpresa}
                        onChange={e => setDatos({ ...datos, nombreEmpresa: e.target.value })}
                      />
                    </div>
                    <div className="space-y-1">
                      <label className="text-[10px] font-black text-[#95A5A6] uppercase ml-2">Pie de página del ticket</label>
                      <textarea
                        className="w-full h-32 p-5 bg-[#F8F9FA] rounded-2xl outline-none font-bold text-[#2C3E50] resize-none"
                        value={datos.mensajeTicket}
                        onChange={e => setDatos({ ...datos, mensajeTicket: e.target.value })}
                      />
                    </div>
                    <label className="w-full h-14 border-2 border-dashed border-[#E0E6ED] rounded-2xl text-[#34495E] font-black text-xs flex items-center justify-center gap-3 cursor-pointer hover:bg-gray-50 transition-colors">
                      <Upload size={18} /> {logoFile ? 'LOGO SELECCIONADO' : 'CARGAR LOGO'}
                      <input type="file" accept="image/png,image/jpeg" className="hidden" onChange={handleLogoChange} />
                    </label>
                    <button
                      onClick={() => setVista('fiscal')}
                      className="text-[#00796B] font-black text-[10px] uppercase tracking-widest hover:underline mt-2"
                    >
                      Volver a datos fiscales
                    </button>
                  </div>
                  <div className="w-64 bg-white border-t-8 border-[#001D3D] p-5 shadow-xl text-[10px] font-mono text-[#2C3E50] flex flex-col items-center">
                    {(logoPreview || datos.rutaLogo) && (
                      <img
                        src={logoPreview || `${API}/uploads/${datos.rutaLogo}`}
                        alt="Logo"
                        className="w-16 h-16 object-contain mb-3 grayscale contrast-125"
                      />
                    )}
                    <div className="text-center mb-6 w-full">
                      <p className="font-black text-xs mb-1 uppercase tracking-tighter">
                        {datos.nombreEmpresa || 'NOMBRE DE LA TIENDA'}
                      </p>
                      <p className="opacity-60">CIF: {datos.cif || '---------'}</p>
                      <p className="opacity-60 uppercase">{datos.direccion || 'DIRECCIÓN'}</p>
                    </div>
                    <div className="border-t border-dashed border-gray-300 my-3 pt-3 w-full">
                      <div className="flex justify-between font-black mb-2 uppercase">
                        <span>CANT. CONCEPTO</span><span>IMPORTE</span>
                      </div>
                      <div className="flex justify-between opacity-70">
                        <span>1x Producto Demo</span><span>10,00 €</span>
                      </div>
                    </div>
                    <div className="border-t-2 border-black my-3 pt-3 flex justify-between font-black text-sm w-full">
                      <span>TOTAL</span><span>10,00 €</span>
                    </div>
                    <div className="text-center mt-6 space-y-3 opacity-60 w-full">
                      <p className="font-bold italic">"{datos.mensajeTicket || 'Gracias por su visita'}"</p>
                      <div className="w-12 h-12 bg-gray-100 mx-auto flex items-center justify-center text-[8px] border border-gray-200 uppercase">QR</div>
                      <p className="text-[7px] tracking-[4px] font-black">SISTEMA VERIFACTU</p>
                    </div>
                  </div>
                </div>
              )}

              <button
                onClick={handleGuardar}
                disabled={loading}
                className="w-full h-16 bg-[#00796B] text-white font-black rounded-2xl mt-10 hover:bg-[#004D40] transition-all flex items-center justify-center gap-3 shadow-lg"
              >
                {loading ? <Loader2 className="animate-spin" /> : <><Check size={20} /> GUARDAR CONFIGURACIÓN</>}
              </button>
            </>
          ) : (
            <div className="py-10 text-center animate-in zoom-in">
              <div className="w-20 h-20 bg-green-100 text-green-500 rounded-full flex items-center justify-center mb-6 mx-auto">
                <CheckCircle2 size={40} />
              </div>
              <h3 className="text-2xl font-black text-slate-800">¡Configuración Guardada!</h3>
              <p className="text-slate-500 mt-2 mb-8 font-medium">
                El sistema VeriFactu y el diseño del ticket han sido actualizados correctamente.
              </p>
              <button
                onClick={resetModal}
                className="w-full h-14 bg-[#001D3D] text-white font-black rounded-2xl hover:bg-black transition-all uppercase text-xs tracking-widest"
              >
                Volver al Panel
              </button>
            </div>
          )}
        </div>
      </div>
    </div>
  );
}