import React, { useState } from 'react';
import {
  X, Printer, Bluetooth, Usb, CheckCircle2,
  Loader2, AlertCircle, ChevronRight, Settings2, Receipt
} from 'lucide-react';

export default function ModalAñadirImpresora({ isOpen, onClose }) {
  const [paso, setPaso] = useState('seleccionar'); // 'seleccionar' | 'buscando' | 'configurar' | 'exito'
  const [tipoConexion, setTipoConexion] = useState(null); // 'bluetooth' | 'usb'
  const [cargando, setCargando] = useState(false);
  const [error, setError] = useState('');

  const [datosImpresora, setDatosImpresora] = useState({
    nombre: '',
    anchoPapel: '80mm',
    dispositivoNativo: null // Aquí se guarda la conexión viva con el hardware
  });

  // --- LÓGICA NATIVA REAL DEL NAVEGADOR ---
  const iniciarBusqueda = async (tipo) => {
    setTipoConexion(tipo);
    setPaso('buscando');
    setError('');

    try {
      if (tipo === 'bluetooth') {
        // 1. Verificamos si el navegador soporta Bluetooth
        if (!navigator.bluetooth) {
          throw new Error("Tu navegador no soporta Bluetooth Web. Usa Google Chrome o Edge.");
        }

        // 2. Abre el buscador nativo de Bluetooth de Windows/Mac/Android
        const device = await navigator.bluetooth.requestDevice({
          acceptAllDevices: true,
          optionalServices: ['000018f0-0000-1000-8000-00805f9b34fb'] // UUID estándar de impresoras ESC/POS
        });

        // 3. Guardamos la referencia de la impresora seleccionada
        setDatosImpresora({
          ...datosImpresora,
          nombre: device.name || 'Impresora Bluetooth Genérica',
          dispositivoNativo: device
        });

      } else if (tipo === 'usb') {
        // 1. Verificamos si el navegador soporta USB Serial
        if (!navigator.serial) {
          throw new Error("Tu navegador no soporta conexión USB Web. Usa Google Chrome o Edge.");
        }

        // 2. Abre el buscador nativo de puertos USB/COM
        const port = await navigator.serial.requestPort();

        // Las impresoras USB a veces no devuelven su nombre comercial fácilmente,
        // así que le asignamos un nombre genérico con su ID de fabricante si existe.
        const info = port.getInfo();
        const nombreUSB = info.usbVendorId
          ? `Impresora Térmica USB (ID: ${info.usbVendorId})`
          : 'Impresora Térmica USB';

        // 3. Guardamos el puerto de la impresora
        setDatosImpresora({
          ...datosImpresora,
          nombre: nombreUSB,
          dispositivoNativo: port
        });
      }

      setPaso('configurar');

    } catch (err) {
      console.error("Error conectando al hardware:", err);
      // Si el usuario le da a "Cancelar" en la ventanita del navegador, cae aquí
      setError(err.message || 'No se pudo conectar o se canceló la búsqueda.');
      setPaso('seleccionar');
    }
  };

  const handleGuardar = () => {
    setCargando(true);

    // Aquí deberíamos guardar la configuración en el estado global (Context)
    // o en localStorage. Ojo: 'dispositivoNativo' no se puede guardar en localStorage
    // porque es un objeto de hardware vivo. Debes mantenerlo en la memoria de React.

    setTimeout(() => {
      setCargando(false);
      setPaso('exito');
    }, 500); // Pequeño delay visual para que se vea que guarda
  };

  const resetModal = () => {
    setPaso('seleccionar');
    setTipoConexion(null);
    setDatosImpresora({ nombre: '', anchoPapel: '80mm', dispositivoNativo: null });
    setError('');
    onClose();
  };

  if (!isOpen) return null;

  return (
    <div className="fixed inset-0 bg-[#001D3D]/40 backdrop-blur-sm flex items-center justify-center z-50 p-4">
      <div className="bg-white rounded-[24px] w-full max-w-xl shadow-2xl overflow-hidden border border-gray-100 animate-in zoom-in-95 duration-200">

        <div className="p-6 flex justify-between items-center bg-white border-b border-gray-50">
          <div className="flex items-center">
            <div className="w-10 h-10 bg-[#E0F2F1] rounded-xl flex items-center justify-center mr-4">
              <Printer className="text-[#00796B]" size={20} />
            </div>
            <h2 className="text-[#001D3D] text-xl font-black tracking-tight">
              Añadir Impresora
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

          {/* PASO 1: SELECCIONAR CONEXIÓN */}
          {paso === 'seleccionar' && (
            <div className="space-y-6">
              <p className="text-slate-500 font-medium">Selecciona el método de conexión de tu impresora térmica de tickets.</p>

              <div className="grid grid-cols-2 gap-4">
                <button
                  onClick={() => iniciarBusqueda('bluetooth')}
                  className="p-6 border-2 border-slate-100 rounded-2xl hover:border-[#00796B] hover:bg-[#E0F2F1]/50 transition-all group flex flex-col items-center text-center"
                >
                  <div className="w-16 h-16 bg-blue-50 text-blue-500 rounded-full flex items-center justify-center mb-4 group-hover:scale-110 transition-transform">
                    <Bluetooth size={32} />
                  </div>
                  <h3 className="font-black text-slate-800 text-lg mb-1">Bluetooth</h3>
                  <p className="text-xs text-slate-500 font-medium">Impresoras inalámbricas emparejadas con este equipo.</p>
                </button>

                <button
                  onClick={() => iniciarBusqueda('usb')}
                  className="p-6 border-2 border-slate-100 rounded-2xl hover:border-[#001D3D] hover:bg-slate-50 transition-all group flex flex-col items-center text-center"
                >
                  <div className="w-16 h-16 bg-slate-100 text-slate-600 rounded-full flex items-center justify-center mb-4 group-hover:scale-110 transition-transform">
                    <Usb size={32} />
                  </div>
                  <h3 className="font-black text-slate-800 text-lg mb-1">Cable USB</h3>
                  <p className="text-xs text-slate-500 font-medium">Impresoras conectadas directamente por cable.</p>
                </button>
              </div>
            </div>
          )}

          {/* PASO 2: BUSCANDO... (Muestra esto muy poco tiempo porque la API es instantánea) */}
          {paso === 'buscando' && (
            <div className="py-12 flex flex-col items-center justify-center text-center">
              <div className="relative mb-6">
                <div className="absolute inset-0 bg-[#00796B] blur-xl opacity-20 rounded-full animate-pulse"></div>
                {tipoConexion === 'bluetooth' ? (
                  <Bluetooth size={64} className="text-[#00796B] relative animate-bounce" />
                ) : (
                  <Usb size={64} className="text-[#001D3D] relative animate-bounce" />
                )}
              </div>
              <h3 className="text-xl font-black text-slate-800 mb-2">Abriendo conector nativo...</h3>
              <p className="text-slate-500">Selecciona tu impresora en la ventana del navegador.</p>
            </div>
          )}

          {/* PASO 3: CONFIGURAR */}
          {paso === 'configurar' && (
            <div className="space-y-6 animate-in slide-in-from-right-4">
              <div className="flex items-center justify-between p-4 bg-green-50 border border-green-100 rounded-xl">
                <div className="flex items-center text-green-700 font-bold">
                  <CheckCircle2 size={20} className="mr-3" />
                  Hardware conectado
                </div>
                <span className="text-xs font-black bg-white px-3 py-1 rounded-full uppercase tracking-widest text-green-600 shadow-sm">
                  {tipoConexion}
                </span>
              </div>

              <div className="space-y-4">
                <div className="space-y-1">
                  <label className="text-[10px] font-black text-[#95A5A6] uppercase ml-2 flex items-center">
                    <Settings2 size={12} className="mr-1" /> Nombre para identificarla
                  </label>
                  <input
                    type="text"
                    value={datosImpresora.nombre}
                    onChange={(e) => setDatosImpresora({...datosImpresora, nombre: e.target.value})}
                    className="w-full h-14 px-5 bg-[#F8F9FA] rounded-2xl border-2 border-transparent focus:border-[#00796B] outline-none font-bold text-[#2C3E50] transition-all"
                  />
                </div>

                <div className="space-y-2">
                  <label className="text-[10px] font-black text-[#95A5A6] uppercase ml-2 flex items-center">
                    <Receipt size={12} className="mr-1" /> Formato del rollo de papel
                  </label>
                  <div className="grid grid-cols-2 gap-3">
                    <button
                      onClick={() => setDatosImpresora({...datosImpresora, anchoPapel: '58mm'})}
                      className={`h-12 rounded-xl font-black text-sm border-2 transition-all ${
                        datosImpresora.anchoPapel === '58mm'
                        ? 'border-[#00796B] bg-[#E0F2F1] text-[#00796B]'
                        : 'border-slate-100 bg-white text-slate-400 hover:border-slate-200'
                      }`}
                    >
                      58mm (Pequeño)
                    </button>
                    <button
                      onClick={() => setDatosImpresora({...datosImpresora, anchoPapel: '80mm'})}
                      className={`h-12 rounded-xl font-black text-sm border-2 transition-all ${
                        datosImpresora.anchoPapel === '80mm'
                        ? 'border-[#00796B] bg-[#E0F2F1] text-[#00796B]'
                        : 'border-slate-100 bg-white text-slate-400 hover:border-slate-200'
                      }`}
                    >
                      80mm (Estándar)
                    </button>
                  </div>
                </div>
              </div>

              <div className="flex gap-3 pt-4">
                <button
                  onClick={() => setPaso('seleccionar')}
                  className="flex-1 h-14 font-black rounded-2xl text-slate-500 bg-slate-100 hover:bg-slate-200 transition-colors uppercase text-xs tracking-widest"
                >
                  Cancelar
                </button>
                <button
                  onClick={handleGuardar}
                  disabled={cargando}
                  className="flex-[2] h-14 bg-[#001D3D] text-white font-black rounded-2xl hover:bg-[#001226] transition-all flex items-center justify-center shadow-lg uppercase text-xs tracking-widest"
                >
                  {cargando ? <Loader2 className="animate-spin" /> : 'Guardar y Finalizar'}
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
              <h3 className="text-2xl font-black text-slate-800 mb-2">¡Hardware Conectado!</h3>
              <p className="text-slate-500 font-medium mb-8">
                {datosImpresora.nombre} ({datosImpresora.anchoPapel}) está lista para recibir tickets.
              </p>
              <button
                onClick={resetModal}
                className="w-full h-14 bg-[#00796B] text-white font-black rounded-2xl hover:bg-[#005d52] transition-all flex items-center justify-center uppercase text-xs tracking-widest shadow-lg"
              >
                Cerrar <ChevronRight size={18} className="ml-2" />
              </button>
            </div>
          )}
        </div>
      </div>
    </div>
  );
}