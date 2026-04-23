import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import axios from 'axios';
import {
  Package, Search, Plus, ChevronDown, ChevronRight,
  Edit, Trash2, Image as ImageIcon, ArrowLeft, CornerDownRight,
  Eye, EyeOff, Archive, ArchiveRestore
} from 'lucide-react';

import ModalAddProducto from './AñadirProducto';
import ModalAddCategoria from './AñadirCategoria';
import ModalImportarProductos from './ImportarProductos';

export default function Inventario() {
  const [busqueda, setBusqueda] = useState('');
  const [categorias, setCategorias] = useState([]);
  const [cargando, setCargando] = useState(true);
  const [error, setError] = useState('');
  const [categoriasAbiertas, setCategoriasAbiertas] = useState({});
  const [mostrarArchivados, setMostrarArchivados] = useState(false);
  const navigate = useNavigate();

  const [modalProdOpen, setModalProdOpen] = useState(false);
  const [modalCatOpen, setModalCatOpen] = useState(false);
  const [modalImportOpen, setModalImportOpen] = useState(false);

  const [prodAEditar, setProdAEditar] = useState(null);
  const [catAEditar, setCatAEditar] = useState(null);
  const [catAImportar, setCatAImportar] = useState(null);

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

      const inicial = {};
      response.data.forEach(cat => {
        const id = cat.idCategoria || cat.id;
        if (!cat.categoriaPadre) inicial[id] = true;
      });
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

  const handleEditProducto = (e, prod, idCategoriaFijada) => {
    e.stopPropagation();
    setProdAEditar({ ...prod, idCategoriaFijada });
    setModalProdOpen(true);
  };

  const handleToggleProductoStatus = async (e, prod) => {
    e.stopPropagation();
    const esActivo = prod.activo !== false;
    const mensaje = esActivo
      ? `¿Seguro que deseas archivar "${prod.nombre}"? No aparecerá en la caja para ventas.`
      : `¿Deseas restaurar "${prod.nombre}" para que vuelva a estar a la venta?`;

    if (window.confirm(mensaje)) {
      try {
        await axios.delete(`http://localhost:8080/api/productos/${prod.codigoBarras}`);
        cargarInventario();
      } catch (error) {
        alert("Error al cambiar el estado del producto.");
      }
    }
  };

  const handleEditCategoria = (e, cat) => {
    e.stopPropagation();
    setCatAEditar(cat);
    setModalCatOpen(true);
  };

  const handleDeleteCategoria = async (e, idCategoria, nombre) => {
    e.stopPropagation();
    if (window.confirm(`¿Seguro que deseas eliminar la categoría "${nombre}"?`)) {
      try {
        await axios.delete(`http://localhost:8080/api/categorias/${idCategoria}`);
        cargarInventario();
      } catch (error) {
        alert("No se puede eliminar una categoría que contiene productos o subcategorías.");
      }
    }
  };

  const handleOpenImportModal = (e, cat) => {
    e.stopPropagation();
    setCatAImportar(cat);
    setModalImportOpen(true);
  };

  const categoriasFiltradas = categorias.filter(cat => {
    const coincideCategoria = cat.nombre.toLowerCase().includes(busqueda.toLowerCase());
    const coincideProducto = cat.productos?.some(p =>
      p.nombre.toLowerCase().includes(busqueda.toLowerCase())
    );
    return coincideCategoria || coincideProducto;
  });

  const categoriasPrincipales = categoriasFiltradas.filter(cat => !cat.categoriaPadre);

  return (
    <div className="min-h-screen bg-slate-50 p-8 font-sans">
      <div className="max-w-6xl mx-auto">

        <div className="flex justify-between items-center mb-10">
          <div className="flex items-center">
            <button onClick={() => navigate('/dashboard')} className="mr-4 p-2 text-slate-500 hover:bg-slate-200 hover:text-slate-800 rounded-full transition-all">
              <ArrowLeft size={24} />
            </button>
            <div className="flex items-center text-slate-800 border-l pl-4 border-slate-200">
              <Package className="w-7 h-7 text-slate-700 mr-3" />
              <h1 className="text-2xl font-bold tracking-tight">Gestión de Inventario</h1>
            </div>
          </div>

          <div className="flex items-center space-x-3">
            <button
              onClick={() => setMostrarArchivados(!mostrarArchivados)}
              className="bg-white border border-slate-200 text-[#001D3D] px-4 py-2 rounded-lg font-bold hover:bg-slate-100 transition-all flex items-center text-xs uppercase tracking-widest shadow-sm"
            >
              {mostrarArchivados ? <Eye size={16} className="mr-2" /> : <EyeOff size={16} className="mr-2" />}
              {mostrarArchivados ? 'Ocultar Inactivos' : 'Ver Inactivos'}
            </button>

            <button onClick={() => { setCatAEditar(null); setModalCatOpen(true); }} className="bg-white border border-slate-200 text-slate-700 px-4 py-2 rounded-lg font-semibold hover:bg-slate-100 transition-all flex items-center text-sm shadow-sm">
              <Plus className="w-4 h-4 mr-2" /> Categoría
            </button>
            <button onClick={() => { setProdAEditar(null); setModalProdOpen(true); }} className="bg-slate-800 text-white px-4 py-2 rounded-lg font-semibold hover:bg-slate-900 transition-all flex items-center text-sm shadow-md">
              <Plus className="w-4 h-4 mr-2" /> Producto
            </button>
          </div>
        </div>

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

        {cargando && <div className="text-center py-20 text-slate-500 animate-pulse">Cargando inventario...</div>}
        {error && <div className="bg-red-50 text-red-600 p-4 rounded-xl text-center border border-red-100">{error}</div>}

        {!cargando && !error && (
          <div className="space-y-4">
            {categoriasPrincipales.map(catPadre => {
              const idPadre = catPadre.idCategoria || catPadre.id;
              const subcategorias = categoriasFiltradas.filter(cat => (cat.categoriaPadre?.id || cat.categoriaPadre?.idCategoria) === idPadre);

              return (
                <div key={idPadre} className="bg-white rounded-2xl border border-slate-200 shadow-sm overflow-hidden transition-all">

                  {/* Header Categoría Padre */}
                  <div className="flex items-center justify-between bg-slate-100/80 p-4 border-b border-slate-200">
                    <div className="flex items-center cursor-pointer flex-1" onClick={() => toggleCategoria(idPadre)}>
                      {categoriasAbiertas[idPadre] ? <ChevronDown className="w-5 h-5 mr-3 text-slate-500" /> : <ChevronRight className="w-5 h-5 mr-3 text-slate-500" />}
                      <span className="font-black text-[#001D3D] text-sm uppercase tracking-wider">{catPadre.nombre}</span>
                    </div>

                    {/* BOTONES ANIMADOS UNIFICADOS (Padre) */}
                    <div className="flex items-center space-x-1">
                      <button onClick={(e) => handleOpenImportModal(e, catPadre)} className="p-2 text-slate-400 hover:text-[#001D3D] hover:bg-slate-200 rounded-lg transition-all" title="Importar productos">
                        <CornerDownRight className="w-5 h-5" />
                      </button>
                      <button onClick={(e) => handleEditCategoria(e, catPadre)} className="p-2 text-slate-400 hover:text-[#001D3D] hover:bg-slate-200 rounded-lg transition-all" title="Editar categoría">
                        <Edit className="w-5 h-5" />
                      </button>
                      <button onClick={(e) => handleDeleteCategoria(e, idPadre, catPadre.nombre)} className="p-2 text-slate-400 hover:text-red-500 hover:bg-red-50 rounded-lg transition-all" title="Eliminar categoría">
                        <Trash2 className="w-5 h-5" />
                      </button>
                    </div>
                  </div>

                  {/* CONTENIDO DEL PADRE */}
                  {categoriasAbiertas[idPadre] && (
                    <div className="divide-y divide-slate-100">

                      {/* 1. SUBCATEGORÍAS */}
                      {subcategorias.map(subcat => {
                        const idSub = subcat.idCategoria || subcat.id;

                        const productosSubcatFiltrados = subcat.productos?.filter(p =>
                          p.nombre.toLowerCase().includes(busqueda.toLowerCase()) &&
                          (mostrarArchivados ? true : p.activo !== false)
                        ) || [];

                        return (
                          <div key={idSub} className="bg-slate-50/50">
                            <div className="flex items-center justify-between p-3 pl-10 border-b border-slate-100 hover:bg-slate-100/50 transition-colors">
                              <div className="flex items-center cursor-pointer flex-1" onClick={() => toggleCategoria(idSub)}>
                                {categoriasAbiertas[idSub] ? <ChevronDown className="w-4 h-4 mr-2 text-slate-400" /> : <ChevronRight className="w-4 h-4 mr-2 text-slate-400" />}
                                <span className="font-bold text-slate-600 text-sm">{subcat.nombre}</span>
                              </div>

                              {/* BOTONES ANIMADOS UNIFICADOS (Subcategoría) */}
                              <div className="flex items-center space-x-1">
                                <button onClick={(e) => handleOpenImportModal(e, subcat)} className="p-2 text-slate-300 hover:text-[#001D3D] hover:bg-slate-200 rounded-lg transition-all" title="Importar productos">
                                  <CornerDownRight className="w-4 h-4" />
                                </button>
                                <button onClick={(e) => handleEditCategoria(e, subcat)} className="p-2 text-slate-300 hover:text-[#001D3D] hover:bg-slate-200 rounded-lg transition-all" title="Editar subcategoría">
                                  <Edit className="w-4 h-4" />
                                </button>
                                <button onClick={(e) => handleDeleteCategoria(e, idSub, subcat.nombre)} className="p-2 text-slate-300 hover:text-red-500 hover:bg-red-50 rounded-lg transition-all" title="Eliminar subcategoría">
                                  <Trash2 className="w-4 h-4" />
                                </button>
                              </div>
                            </div>

                            {/* PRODUCTOS DE LA SUBCATEGORÍA */}
                            {categoriasAbiertas[idSub] && (
                              <div className="divide-y divide-slate-50">
                                {productosSubcatFiltrados.map(prod => (
                                  <ProductoFila
                                    key={prod.codigoBarras}
                                    prod={prod} indent="pl-16"
                                    onEdit={(e) => handleEditProducto(e, prod, idSub)}
                                    onToggleStatus={(e) => handleToggleProductoStatus(e, prod)}
                                  />
                                ))}
                                {productosSubcatFiltrados.length === 0 && <p className="py-2 pl-20 text-slate-400 text-[11px] italic">No hay productos visibles</p>}
                              </div>
                            )}
                          </div>
                        );
                      })}

                      {/* 2. PRODUCTOS DIRECTOS DEL PADRE */}
                      {(() => {
                        const productosPadreFiltrados = catPadre.productos?.filter(p =>
                          p.nombre.toLowerCase().includes(busqueda.toLowerCase()) &&
                          (mostrarArchivados ? true : p.activo !== false)
                        ) || [];

                        return productosPadreFiltrados.map(prod => (
                          <ProductoFila
                            key={prod.codigoBarras}
                            prod={prod} indent="pl-10"
                            onEdit={(e) => handleEditProducto(e, prod, idPadre)}
                            onToggleStatus={(e) => handleToggleProductoStatus(e, prod)}
                          />
                        ));
                      })()}
                    </div>
                  )}
                </div>
              );
            })}
          </div>
        )}
      </div>

      <ModalAddProducto isOpen={modalProdOpen} onClose={() => { setModalProdOpen(false); setProdAEditar(null); }} onSuccess={cargarInventario} productoEdit={prodAEditar} />
      <ModalAddCategoria isOpen={modalCatOpen} onClose={() => { setModalCatOpen(false); setCatAEditar(null); }} onSuccess={cargarInventario} categoriaEdit={catAEditar} />
      <ModalImportarProductos isOpen={modalImportOpen} onClose={() => { setModalImportOpen(false); setCatAImportar(null); }} onSuccess={cargarInventario} categoriaEdit={catAImportar} />
    </div>
  );
}

function ProductoFila({ prod, indent, onEdit, onToggleStatus }) {
  const [imgError, setImgError] = useState(false);

  return (
    <div className={`flex items-center justify-between p-4 hover:bg-slate-50/80 transition-colors ${indent} ${prod.activo === false ? 'opacity-50 grayscale' : ''}`}>
      <div className="flex items-center">
        <div className="w-12 h-12 rounded-xl flex items-center justify-center mr-4 border border-slate-200 bg-white shadow-sm overflow-hidden shrink-0">
          {prod.urlImagen && !imgError ? (
            <img
              src={`http://localhost:8080/uploads/${prod.urlImagen}`}
              alt={prod.nombre}
              className="w-full h-full object-cover"
              onError={() => setImgError(true)}
            />
          ) : (
            <ImageIcon className="text-slate-300 w-6 h-6" />
          )}
        </div>
        <div>
          <p className="font-bold text-slate-700 text-sm">
            {prod.nombre} {prod.activo === false && <span className="text-[10px] bg-slate-200 px-1.5 py-0.5 rounded ml-2 text-slate-500">INACTIVO</span>}
          </p>
          <p className="text-xs font-semibold text-teal-600">{prod.precio.toFixed(2)} €</p>
        </div>
      </div>

      <div className="flex items-center space-x-6">
        <div className="text-right">
          <p className="text-[10px] font-bold text-slate-400 uppercase tracking-wider mb-0.5">Stock</p>
          <span className={`px-2 py-1 rounded-md text-xs font-black ${prod.unidades < 5 ? 'bg-red-100 text-red-600' : 'bg-slate-100 text-slate-600'}`}>
            {prod.unidades}
          </span>
        </div>

        {/* BOTONES ANIMADOS UNIFICADOS (Producto) */}
        <div className="flex items-center space-x-1 border-l pl-5 border-slate-200">
          <button
            onClick={onEdit}
            className="p-2 text-slate-300 hover:text-[#001D3D] hover:bg-slate-100 rounded-lg transition-all"
            title="Editar producto"
          >
            <Edit className="w-4 h-4" />
          </button>

          <button
            onClick={onToggleStatus}
            className={`p-2 rounded-lg transition-all ${
              prod.activo === false
              ? 'text-slate-400 hover:text-[#001D3D] hover:bg-slate-100'
              : 'text-slate-300 hover:text-red-500 hover:bg-red-50'
            }`}
            title={prod.activo === false ? "Restaurar producto" : "Archivar producto"}
          >
            {prod.activo === false ? <ArchiveRestore className="w-4 h-4" /> : <Archive className="w-4 h-4" />}
          </button>
        </div>
      </div>
    </div>
  );
}