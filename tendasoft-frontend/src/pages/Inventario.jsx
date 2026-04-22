import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import axios from 'axios';
import {
  Package, Search, Plus, ChevronDown, ChevronRight,
  Edit, Trash2, Image as ImageIcon, Tag, ArrowLeft
} from 'lucide-react';

// Importamos los nuevos modales
import ModalAddProducto from './AñadirProducto';
import ModalAddCategoria from './AñadirCategoria';

export default function Inventario() {
  const [busqueda, setBusqueda] = useState('');
  const [categorias, setCategorias] = useState([]);
  const [cargando, setCargando] = useState(true);
  const [error, setError] = useState('');
  const [categoriasAbiertas, setCategoriasAbiertas] = useState({});
  const navigate = useNavigate();

  // Estados para controlar la visibilidad de los modales
  const [modalProdOpen, setModalProdOpen] = useState(false);
  const [modalCatOpen, setModalCatOpen] = useState(false);

  useEffect(() => {
    const auth = JSON.parse(localStorage.getItem('usuarioTendaSoft'));
    if (!auth || auth.rol !== 'ADMIN') {
      navigate('/dashboard');
      return;
    }

    cargarInventario();
  }, [navigate]);

  const cargarInventario = async () => {
    try {
      setCargando(true);
      const response = await axios.get('http://localhost:8080/api/categorias');
      setCategorias(response.data);

      // Abrir todas las categorías por defecto al cargar
      const inicial = {};
      response.data.forEach(cat => inicial[cat.idCategoria] = true);
      setCategoriasAbiertas(inicial);

    } catch (err) {
      setError('No se pudo conectar con el servidor de inventario.');
    } finally {
      setCargando(false);
    }
  };

  const toggleCategoria = (id) => {
    setCategoriasAbiertas(prev => ({ ...prev, [id]: !prev[id] }));
  };

  const categoriasFiltradas = categorias.filter(cat => {
    const coincideCategoria = cat.nombre.toLowerCase().includes(busqueda.toLowerCase());
    const coincideProducto = cat.productos?.some(p =>
      p.nombre.toLowerCase().includes(busqueda.toLowerCase())
    );
    return coincideCategoria || coincideProducto;
  });

  return (
    <div className="min-h-screen bg-slate-50 p-8 font-sans">
      <div className="max-w-6xl mx-auto">

        {/* Cabecera - Botones conectados a los modales */}
        <div className="flex justify-between items-center mb-10">
          {/* IZQUIERDA: Navegación + Título */}
          <div className="flex items-center">
            <button
              onClick={() => navigate('/dashboard')}
              className="mr-4 p-2 text-slate-500 hover:bg-slate-200 hover:text-slate-800 rounded-full transition-all"
              title="Volver"
            >
              <ArrowLeft size={24} />
            </button>

            <div className="flex items-center text-slate-800 border-l pl-4 border-slate-200">
              <Package className="w-7 h-7 text-slate-700 mr-3" />
              <h1 className="text-2xl font-bold tracking-tight">Gestión de Inventario</h1>
            </div>
          </div>

          {/* DERECHA: Acciones principales en la misma línea */}
          <div className="flex items-center space-x-3">
            <button
              onClick={() => setModalCatOpen(true)}
              className="bg-white border border-slate-200 text-slate-700 px-4 py-2 rounded-lg font-semibold hover:bg-slate-100 transition-all flex items-center text-sm shadow-sm"
            >
              <Plus className="w-4 h-4 mr-2" /> Categoría
            </button>
            <button
              onClick={() => setModalProdOpen(true)}
              className="bg-slate-800 text-white px-4 py-2 rounded-lg font-semibold hover:bg-slate-900 transition-all flex items-center text-sm shadow-md"
            >
              <Plus className="w-4 h-4 mr-2" /> Producto
            </button>
          </div>
        </div>

        {/* Buscador */}
        <div className="relative mb-8 max-w-2xl mx-auto">
          <Search className="absolute left-4 top-1/2 -translate-y-1/2 text-slate-400 w-5 h-5" />
          <input
            type="text"
            placeholder="Buscar por producto o categoría..."
            value={busqueda}
            onChange={(e) => setBusqueda(e.target.value)}
            className="w-full pl-12 pr-4 py-3.5 bg-white border border-slate-200 rounded-2xl shadow-sm focus:outline-none focus:ring-2 focus:ring-slate-300 transition-all"
          />
        </div>

        {/* Estado de carga o error */}
        {cargando && <div className="text-center py-20 text-slate-500 animate-pulse">Cargando inventario...</div>}
        {error && <div className="bg-red-50 text-red-600 p-4 rounded-xl text-center border border-red-100">{error}</div>}

        {/* Lista Acordeón */}
        {!cargando && !error && (
          <div className="space-y-4">
            {categoriasFiltradas.map(cat => (
              <div key={cat.idCategoria} className="bg-white rounded-2xl border border-slate-100 shadow-sm overflow-hidden">

                {/* Header Categoría */}
                <div className="flex items-center justify-between bg-slate-50/50 p-4 border-b border-slate-50">
                  <div
                    className="flex items-center cursor-pointer flex-1"
                    onClick={() => toggleCategoria(cat.idCategoria)}
                  >
                    {categoriasAbiertas[cat.idCategoria] ?
                      <ChevronDown className="w-5 h-5 mr-3 text-slate-400" /> :
                      <ChevronRight className="w-5 h-5 mr-3 text-slate-400" />
                    }
                    <span className="font-bold text-slate-700 text-sm uppercase tracking-wider">{cat.nombre}</span>
                  </div>

                  <div className="flex items-center space-x-5 text-slate-400">
                    <Plus className="w-5 h-5 cursor-pointer hover:text-slate-700 transition-colors" title="Añadir producto a esta categoría" />
                    <Tag className="w-5 h-5 cursor-pointer hover:text-slate-700 transition-colors" />
                    <Edit className="w-5 h-5 cursor-pointer hover:text-slate-700 transition-colors" />
                    <Trash2 className="w-5 h-5 cursor-pointer hover:text-red-500 transition-colors" />
                  </div>
                </div>

                {/* Productos */}
                {categoriasAbiertas[cat.idCategoria] && (
                  <div className="divide-y divide-slate-50">
                    {cat.productos?.length > 0 ? (
                      cat.productos
                        .filter(p => p.nombre.toLowerCase().includes(busqueda.toLowerCase()))
                        .map(prod => (
                          <div key={prod.idProducto} className="flex items-center justify-between p-5 hover:bg-slate-50/30 transition-colors">
                            <div className="flex items-center">
                              <div className="w-12 h-12 rounded-xl flex items-center justify-center mr-4 border border-slate-100 bg-slate-50">
                                <ImageIcon className="text-slate-300 w-6 h-6" />
                              </div>
                              <div>
                                <p className="font-bold text-slate-800">{prod.nombre}</p>
                                <p className="text-sm font-medium text-slate-400">{prod.precio.toFixed(2)} €</p>
                              </div>
                            </div>

                            <div className="flex items-center space-x-10">
                              <div className="text-right">
                                <p className="text-xs font-bold text-slate-400 uppercase">Stock</p>
                                <p className={`font-black ${prod.unidades < 5 ? 'text-red-500' : 'text-slate-700'}`}>x{prod.unidades}</p>
                              </div>
                              <div className="flex items-center space-x-4 text-slate-300 border-l pl-6 border-slate-100">
                                <Plus className="w-5 h-5 cursor-pointer hover:text-slate-600 transition-colors" />
                                <Edit className="w-5 h-5 cursor-pointer hover:text-slate-600 transition-colors" />
                                <Trash2 className="w-5 h-5 cursor-pointer hover:text-red-400 transition-colors" />
                              </div>
                            </div>
                          </div>
                        ))
                    ) : (
                      <p className="p-6 text-center text-slate-400 text-sm">No hay productos en esta categoría.</p>
                    )}
                  </div>
                )}
              </div>
            ))}
          </div>
        )}
      </div>

      {/* MODALES - Conectados a la función cargarInventario */}
      <ModalAddProducto
        isOpen={modalProdOpen}
        onClose={() => setModalProdOpen(false)}
        onSuccess={cargarInventario}
      />

      <ModalAddCategoria
        isOpen={modalCatOpen}
        onClose={() => setModalCatOpen(false)}
        onSuccess={cargarInventario}
      />
    </div>
  );
}