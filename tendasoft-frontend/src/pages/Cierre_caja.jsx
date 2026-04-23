import React, { useState, useEffect } from 'react';
import axios from 'axios';
import { Lock, Unlock, AlertCircle, CheckCircle2, ArrowLeft } from 'lucide-react';
import { useNavigate } from 'react-router-dom';

export default function CierreCaja() {
  const navigate = useNavigate();

  // --- ESTADOS ---
  const [fondoInicial, setFondoInicial] = useState('');
  const [efectivoContado, setEfectivoContado] = useState('');
  const [notas, setNotas] = useState('');
  const [resumen, setResumen] = useState({
    totalVentas: 0.0,
    efectivo: 0.0,
    tarjeta: 0.0,
    otros: 0.0
  });
  const [estaAbierta, setEstaAbierta] = useState(false);

  const [fecha] = useState(new Date().toLocaleDateString('es-ES', {
    weekday: 'long', year: 'numeric', month: 'long', day: 'numeric'
  }));

  // --- INTEGRACIÓN BACKEND ---
  useEffect(() => {
    const cargarDatosCaja = async () => {
      try {
        const auth = JSON.parse(localStorage.getItem('usuarioTendaSoft'));
        if (!auth) return;

        const userId = auth.idUsuario || auth.id;

        // 1. Obtenemos el estado de la caja
        const resEstado = await axios.get(`http://localhost:8080/api/caja/estado/${userId}`);

        if (resEstado.data && resEstado.data.id) { // Si existe una caja abierta
          setEstaAbierta(true);

          setFondoInicial(resEstado.data.fondoInicial);

          // 2. Cargamos el resumen de ventas
          const resResumen = await axios.get(`http://localhost:8080/api/caja/resumen/${userId}`);
          setResumen({
            totalVentas: resResumen.data.totalVentas || 0,
            efectivo: resResumen.data.efectivo || 0,
            tarjeta: resResumen.data.tarjeta || 0,
            otros: resResumen.data.otros || 0
          });
        }
      } catch (err) {
        console.error("Error al recuperar estado de caja", err);
      }
    };
    cargarDatosCaja();
  }, []);

  // --- APERTURA DE CAJA (Estructura Postman) ---
  const handleAbrirCaja = async () => {
    try {
      const auth = JSON.parse(localStorage.getItem('usuarioTendaSoft'));

      // Estructura JSON exacta a tu petición de Postman
      const peticion = {
        fondoInicial: parseFloat(fondoInicial),
        usuario: {
          idUsuario: auth.idUsuario
        }
      };

      await axios.post('http://localhost:8080/api/caja/abrir', peticion);
      setEstaAbierta(true);
      alert("Caja abierta correctamente");
    } catch (error) {
      alert("Error: " + (error.response?.data?.message || "No se pudo abrir la caja"));
    }
  };

  // --- CIERRE DE CAJA ---
  const handleCerrarCaja = async () => {
    try {
      const auth = JSON.parse(localStorage.getItem('usuarioTendaSoft'));
      const peticion = {
        idUsuario: auth.idUsuario,
        dineroFisicoContado: parseFloat(efectivoContado)
      };

      const res = await axios.post('http://localhost:8080/api/caja/cerrar', peticion);
      alert(`Caja cerrada correctamente.\nDescuadre: ${res.data.descuadre} €`);

      // Reiniciamos la pantalla
      setEstaAbierta(false);
      setFondoInicial('');
      setEfectivoContado('');
      setNotas('');
      setResumen({ totalVentas: 0, efectivo: 0, tarjeta: 0, otros: 0 });
    } catch (error) {
      alert("Error al cerrar: " + (error.response?.data?.message || "Error desconocido"));
    }
  };

  return (
    // Pantalla completa bloqueada (h-screen, sin scroll general)
    <div className="h-screen w-screen overflow-hidden bg-[#F4F7F9] p-6 flex flex-col font-sans">

      {/* Cabecera superior con botón de volver */}
      <div className="flex items-center justify-between mb-8 flex-shrink-0">
        {/* IZQUIERDA: Botón de navegación + Título e Icono */}
        <div className="flex items-center">
          {/* Botón Volver circular y sutil */}
          <button
            onClick={() => navigate('/dashboard')}
            className="mr-4 p-2 text-slate-500 hover:bg-slate-200 hover:text-slate-800 rounded-full transition-all"
            title="Volver al inicio"
          >
            <ArrowLeft size={24} />
          </button>

          {/* Divisor vertical y bloque de contenido */}
          <div className="flex items-center text-[#2C3E50] border-l pl-4 border-slate-200">
            {/* Candado alineado con el texto */}
            <Lock className="mr-4 text-[#2C3E50]" size={32} />

            <div>
              <h1 className="text-3xl font-bold tracking-tight leading-none">
                Apertura y Cierre
              </h1>
              <p className="text-[#7F8C8D] text-sm mt-1 font-medium capitalize">
                Día en curso: {fecha}
              </p>
            </div>
          </div>
        </div>
      </div>

      {/* Contenedor Principal (Expande para llenar la pantalla) */}
      <div className="flex-1 flex gap-6 w-full max-w-7xl mx-auto">

        {/* === COLUMNA IZQUIERDA (48%) === */}
        <div className="w-[48%] flex flex-col gap-6 h-full">

          {/* Tarjeta 1: Apertura */}
          <div className="bg-white rounded-[16px] shadow-sm p-8 flex-shrink-0">
            <h2 className="text-[#2C3E50] text-xl font-bold mb-6 flex items-center">
              <Unlock className="w-6 h-6 mr-3 text-[#F39C12]" /> 1. Apertura de Caja
            </h2>

            <div className="flex items-center space-x-4">
              <input
                type="number"
                placeholder="Fondo inicial (€)"
                value={fondoInicial}
                onChange={(e) => setFondoInicial(e.target.value)}
                disabled={estaAbierta}
                className="flex-1 h-[60px] px-4 border border-gray-200 rounded-[12px] text-lg font-bold text-[#2C3E50] placeholder-[#95A5A6] focus:outline-none focus:ring-2 focus:ring-[#F39C12] disabled:bg-slate-50 disabled:text-slate-400"
              />
              <button
                onClick={handleAbrirCaja}
                disabled={estaAbierta || !fondoInicial}
                className="h-[60px] px-8 rounded-[12px] font-bold text-lg text-[#F39C12] border-2 border-[#F39C12] hover:bg-orange-50 active:scale-95 transition-all disabled:opacity-50 disabled:cursor-not-allowed whitespace-nowrap"
              >
                ABRIR CAJA
              </button>
            </div>
          </div>

          {/* Tarjeta 2: Resumen del Turno */}
          <div className="bg-white rounded-[16px] shadow-sm p-8 flex-1 flex flex-col">
            <h2 className="text-[#34495E] text-xl font-bold mb-6">Resumen del turno</h2>

            {/* Cambia esto en el resumen de ventas del ticket o de la tarjeta de resumen */}

            <span className="text-[#2C3E50] font-bold">
              {(resumen.efectivo || 0).toFixed(2)} €
            </span>

            <span className="text-[#2C3E50] font-bold">
              {(resumen.tarjeta || 0).toFixed(2)} €
            </span>

            <span className="text-[#2C3E50] font-bold">
              {(resumen.otros || 0).toFixed(2)} €
            </span>

            <span className="text-[#1976D2] text-[22px] font-black">
              {(resumen.totalVentas || 0).toFixed(2)} €
            </span>

            <div className="h-[2px] bg-[#E0E6ED] w-full my-6" />

            <div className="flex justify-between items-center">
              <span className="text-[#34495E] text-lg font-bold tracking-wider">TOTAL VENTAS</span>
              <span className="text-[#1976D2] text-3xl font-black">{resumen.totalVentas.toFixed(2)} €</span>
            </div>
          </div>

        </div>


        {/* === COLUMNA DERECHA (52%) === */}
        <div className="flex-1 flex flex-col gap-6 h-full">

          {/* Tarjeta 3: Cierre */}
          <div className="bg-white rounded-[16px] shadow-sm p-8 flex-1 flex flex-col">
            <h2 className="text-[#2C3E50] text-xl font-bold mb-6 flex items-center">
              <Lock className="w-6 h-6 mr-3 text-[#F39C12]" /> 2. Cierre de Caja
            </h2>

            <input
              type="number"
              placeholder="Efectivo contado en el cajón (€)"
              value={efectivoContado}
              onChange={(e) => setEfectivoContado(e.target.value)}
              disabled={!estaAbierta}
              className="w-full h-[60px] px-4 mb-6 border border-gray-200 rounded-[12px] text-lg font-bold text-[#2C3E50] placeholder-[#95A5A6] focus:outline-none focus:ring-2 focus:ring-[#F39C12] disabled:bg-slate-50"
            />

            <textarea
              placeholder="Notas (opcional)"
              value={notas}
              onChange={(e) => setNotas(e.target.value)}
              disabled={!estaAbierta}
              className="w-full flex-1 p-4 mb-6 border border-gray-200 rounded-[12px] text-lg text-[#2C3E50] placeholder-[#95A5A6] focus:outline-none focus:ring-2 focus:ring-[#F39C12] disabled:bg-slate-50 resize-none"
            />

            <button
              onClick={handleCerrarCaja}
              disabled={!estaAbierta || !efectivoContado}
              className="w-full h-[72px] bg-[#F39C12] text-white rounded-[12px] font-black text-xl hover:bg-[#e67e22] active:scale-95 transition-all shadow-md disabled:opacity-50 disabled:cursor-not-allowed flex items-center justify-center tracking-wider"
            >
              <Lock className="w-6 h-6 mr-3" /> CERRAR CAJA
            </button>
          </div>

          {/* Tarjeta 4: Estado (Fija abajo) */}
          <div className="bg-[#F8F9FA] rounded-[16px] border-2 border-[#E0E6ED] p-6 flex items-center flex-shrink-0 h-[100px]">
            {estaAbierta ? (
              <>
                <CheckCircle2 className="w-10 h-10 text-[#2ECC71] mr-4" />
                <span className="text-[#2C3E50] font-bold text-xl">Caja actualmente ABIERTA. Lista para operar.</span>
              </>
            ) : (
              <>
                <AlertCircle className="w-10 h-10 text-[#7F8C8D] mr-4" />
                <span className="text-[#2C3E50] font-bold text-xl">Caja CERRADA. Introducir fondo inicial.</span>
              </>
            )}
          </div>

        </div>

      </div>
    </div>
  );
}