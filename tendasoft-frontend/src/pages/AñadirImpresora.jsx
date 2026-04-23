import React, { useState, useEffect } from 'react';
import {
  X, Printer, CheckCircle2, Loader2, AlertCircle, ChevronRight, Settings2, Receipt, Search, Check
} from 'lucide-react';

export default function ModalAñadirImpresora({ isOpen, onClose }) {
  const [paso, setPaso] = useState('inicio'); // 'inicio' | 'buscando' | 'configurar' | 'exito'
  const [cargando, setCargando] = useState(false);
  const [error, setError] = useState('');
  const [listaImpresoras, setListaImpresoras] = useState([]);

  const [datosImpresora, setDatosImpresora] = useState({
    nombreSistema: '', // El nombre real que le da Windows/Mac
    nombreMostrar: '', // Un apodo que le pone el usuario (ej: "Impresora Barra")
    anchoPapel: '80mm',
  });

  // --- 1. CARGA INICIAL DESDE LOCALSTORAGE ---
  useEffect(() => {
    if (isOpen) {
      const guardado = localStorage.getItem('impresoraTendaSoft');
      if (guardado) {
        setDatosImpresora(JSON.parse(guardado));
        // Si ya hay algo guardado, podemos saltar directamente al paso de configurar
        setPaso('configurar');
      }
    }
  }, [isOpen]);

  // --- 2. LLAMADA A ELECTRON A TRAVÉS DEL PUENTE ---
  const iniciarBusqueda = async () => {
    setPaso('buscando');
    setError('');

    try {
      // Usamos el puente que creamos en preload.cjs
      if (!window.impresoraAPI) {
        throw new Error("No se detectó el entorno de escritorio (Electron).");
      }

      const encontradas = await window.impresoraAPI.buscarImpresoras();

      if (encontradas.length === 0) {
         throw new Error("No se encontraron impresoras. Asegúrate de instalarla en Windows/Mac primero.");
      }

      setListaImpresoras(encontradas);

      // Seteamos el estado inicial con la primera impresora encontrada
      // (Respetando el ancho de papel si ya había uno configurado)
      setDatosImpresora(prev => ({
        ...prev,
        nombreSistema: prev.nombreSistema || encontradas[0].nombre,
        nombreMostrar: prev.nombreMostrar || encontradas[0].nombre,
        anchoPapel: prev.anchoPapel || '80mm'
      }));

      setPaso('configurar');
    } catch (err) {
      setError(err.message);
      setPaso('inicio');
    }
  };

  // --- 3. GUARDADO PERSISTENTE ---
  const handleGuardar = () => {
    setCargando(true);

    try {
      // Guardamos el objeto completo en LocalStorage
      localStorage.setItem('impresoraTendaSoft', JSON.stringify(datosImpresora));
      console.log("✅ Configuración guardada en LocalStorage:", datosImpresora);

      setTimeout(() => {
        setCargando(false);
        setPaso('exito');
      }, 600);
    } catch (err) {
      console.error("Error al guardar en LocalStorage", err);
      setError("No se pudo guardar la configuración en el dispositivo.");
      setCargando(false);
    }
  };

  const resetModal = () => {
    setPaso('inicio');
    setListaImpresoras([]);
    setError('');
    onClose();
  };

  if (!isOpen) return null;

  return (
    <div className="fixed inset-0 bg-[#001D3D]/40 backdrop-blur-sm flex items-center justify-center z-50 p-4">
      <div className="bg-white rounded-[24px] w-full max-w-xl shadow-2xl overflow-hidden animate-in zoom-in-95 duration-200">

        <div className="p-6 flex justify-between items-center bg-white border-b border-gray-50">
          <div className="flex items-center">
            <div className="w-10 h-10 bg-[#E0F2F1] rounded-xl flex items-center justify-center mr-4">
              <Printer className="text-[#00796B]" size={20} />
            </div>
            <h2 className="text-[#001D3D] text-xl font-black tracking-tight">
              Añadir Impresora TPV
            </h2>
          </div>
          <button onClick={resetModal} className="p-2 hover:bg-gray-100 rounded-full transition-colors">
            <X size={24} className="text-[#001D3D]" />
          </button>
        </div>

        <div className="p-8">
          {error && (
            <div className="mb-6 p-4 bg-red-50 border border-red-100 rounded-xl flex items-center text-red-600 text-sm font-bold">
              <AlertCircle size={18} className="mr-3 shrink-0" /> {error}
            </div>
          )}

          {/* PASO 1: INICIO */}
          {paso === 'inicio' && (
            <div className="text-center space-y-6 py-4">
              <div className="w-20 h-20 bg-slate-50 text-slate-400 rounded-full flex items-center justify-center mx-auto">
                <Search size={32} />
              </div>
              <div>
                <h3 className="font-black text-slate-800 text-lg mb-2">Buscar impresoras instaladas</h3>
                <p className="text-sm text-slate-500 font-medium px-8">
                  El sistema buscará automáticamente cualquier impresora conectada por USB, Bluetooth o Red que esté instalada en este ordenador.
                </p>
              </div>
              <button
                onClick={iniciarBusqueda}
                className="w-full h-14 bg-[#001D3D] text-white font-black rounded-2xl hover:bg-[#001226] transition-all flex items-center justify-center shadow-lg uppercase text-xs tracking-widest"
              >
                Escanear Dispositivos
              </button>
            </div>
          )}

          {/* PASO 2: BUSCANDO */}
          {paso === 'buscando' && (
            <div className="py-12 flex flex-col items-center justify-center text-center">
              <Loader2 size={48} className="text-[#00796B] animate-spin mb-4" />
              <h3 className="text-xl font-black text-slate-800 mb-2">Consultando al sistema...</h3>
            </div>
          )}

          {/* PASO 3: CONFIGURAR */}
          {paso === 'configurar' && (
            <div className="space-y-6 animate-in slide-in-from-right-4">
              <div className="space-y-1">
                <label className="text-[10px] font-black text-[#95A5A6] uppercase ml-2 flex items-center">
                  <Printer size={12} className="mr-1" /> Selecciona tu impresora
                </label>
                <select
                  value={datosImpresora.nombreSistema}
                  onChange={(e) => setDatosImpresora({...datosImpresora, nombreSistema: e.target.value, nombreMostrar: e.target.value})}
                  className="w-full h-14 px-5 bg-[#F8F9FA] rounded-2xl border-2 border-transparent focus:border-[#00796B] outline-none font-bold text-[#2C3E50]"
                >
                  {/* Si hay impresoras cargadas en el momento, las mostramos. Si venimos del LocalStorage directamente, mostramos la guardada como opción por defecto */}
                  {listaImpresoras.length > 0 ? (
                    listaImpresoras.map((imp, idx) => (
                      <option key={idx} value={imp.nombre}>{imp.descripcion}</option>
                    ))
                  ) : (
                    <option value={datosImpresora.nombreSistema}>{datosImpresora.nombreMostrar}</option>
                  )}
                </select>
                {listaImpresoras.length === 0 && (
                   <button onClick={iniciarBusqueda} className="text-xs text-[#00796B] font-bold mt-2 ml-2 hover:underline">
                     Volver a escanear dispositivos
                   </button>
                )}
              </div>

              <div className="space-y-1">
                <label className="text-[10px] font-black text-[#95A5A6] uppercase ml-2 flex items-center">
                  <Settings2 size={12} className="mr-1" /> Nombre para el TPV (Opcional)
                </label>
                <input
                  type="text"
                  value={datosImpresora.nombreMostrar}
                  onChange={(e) => setDatosImpresora({...datosImpresora, nombreMostrar: e.target.value})}
                  placeholder="Ej: Impresora Principal"
                  className="w-full h-14 px-5 bg-[#F8F9FA] rounded-2xl border-2 border-transparent focus:border-[#00796B] outline-none font-bold text-[#2C3E50]"
                />
              </div>

              <div className="space-y-2">
                <label className="text-[10px] font-black text-[#95A5A6] uppercase ml-2 flex items-center">
                  <Receipt size={12} className="mr-1" /> Formato de papel
                </label>
                <div className="grid grid-cols-2 gap-3">
                  <button
                    onClick={() => setDatosImpresora({...datosImpresora, anchoPapel: '58mm'})}
                    className={`h-12 rounded-xl font-black text-sm border-2 transition-all ${
                      datosImpresora.anchoPapel === '58mm' ? 'border-[#00796B] bg-[#E0F2F1] text-[#00796B]' : 'border-slate-100 text-slate-400 hover:border-slate-200'
                    }`}
                  >
                    58mm (Pequeño)
                  </button>
                  <button
                    onClick={() => setDatosImpresora({...datosImpresora, anchoPapel: '80mm'})}
                    className={`h-12 rounded-xl font-black text-sm border-2 transition-all ${
                      datosImpresora.anchoPapel === '80mm' ? 'border-[#00796B] bg-[#E0F2F1] text-[#00796B]' : 'border-slate-100 text-slate-400 hover:border-slate-200'
                    }`}
                  >
                    80mm (Estándar)
                  </button>
                </div>
              </div>

              <div className="flex gap-3 pt-4">
                <button
                  onClick={() => setPaso('inicio')}
                  className="flex-1 h-14 font-black rounded-2xl text-slate-500 bg-slate-100 hover:bg-slate-200 transition-colors uppercase text-xs tracking-widest"
                >
                  Atrás
                </button>
                <button
                  onClick={handleGuardar}
                  disabled={cargando}
                  className="flex-[2] h-14 bg-[#00796B] text-white font-black rounded-2xl hover:bg-[#005d52] transition-all flex items-center justify-center gap-3 shadow-lg shadow-[#00796B]/20"
                >
                  {cargando ? <Loader2 className="animate-spin" /> : <><Check size={20} /> GUARDAR CONFIGURACIÓN</>}
                </button>
              </div>
            </div>
          )}

          {/* PASO 4: ÉXITO */}
          {paso === 'exito' && (
            <div className="py-10 flex flex-col items-center justify-center text-center animate-in zoom-in">
              <div className="w-20 h-20 bg-green-100 text-green-500 rounded-full flex items-center justify-center mb-6">
                <CheckCircle2 size={40} />
              </div>
              <h3 className="text-2xl font-black text-slate-800 mb-2">¡Configuración Guardada!</h3>
              <p className="text-slate-500 font-medium mb-8">
                El sistema usará "{datosImpresora.nombreMostrar}" para imprimir los tickets.
              </p>
              <button
                onClick={resetModal}
                className="w-full h-14 bg-[#001D3D] text-white font-black rounded-2xl hover:bg-[#001226] transition-all flex items-center justify-center uppercase text-xs tracking-widest shadow-lg"
              >
                Cerrar y Volver
              </button>
            </div>
          )}
        </div>
      </div>
    </div>
  );
}