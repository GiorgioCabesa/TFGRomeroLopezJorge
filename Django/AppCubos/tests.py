from django.test import TestCase, Client
from django.urls import reverse
from django.core.files.uploadedfile import SimpleUploadedFile
from django.conf import settings
import pyrebase
import os
import shutil

class SignInTest(TestCase):
    
    # Metodo prinicipal para hacer la configuración inicial necesaria para los test
    def setUp(self):
        # Configura el cliente para las pruebas
        self.client = Client()
        
        # Configuración de Firebase para pruebas
        self.firebase_config = {
            "apiKey": "AIzaSyDRzAi6Ndx-jamSUh1JdmNMWoZ599gIs_c",
            "authDomain": "tensorcube-ec4ef.firebaseapp.com",
            "projectId": "tensorcube-ec4ef",
            "storageBucket": "tensorcube-ec4ef.appspot.com",
            "messagingSenderId": "95309594224",
            "appId": "1:95309594224:web:ce71cebea298865a853eb5",
            "databaseURL": "https://tensorcube-ec4ef-default-rtdb.europe-west1.firebasedatabase.app"
        }
        self.firebase = pyrebase.initialize_app(self.firebase_config)
        self.authe = self.firebase.auth()
        self.db = self.firebase.database()
        self.storage = self.firebase.storage()
        
        # Crear un usuario de prueba en Firebase
        self.email = "test@example.com"
        self.password = "A123.si"
        self.user = self.authe.create_user_with_email_and_password(self.email, self.password)
        
        # Crear una sesión de usuario
        self.client.post(reverse('postsignIn'), {
            'email': self.email,
            'pass': self.password
        })
        
        # Crear un directorio temporal para pruebas
        self.temp_dir = os.path.join(settings.MEDIA_ROOT, 'test_temp')
        os.makedirs(self.temp_dir)
        
    def tearDown(self):
        # Eliminar el usuario de prueba en Firebase después de cada prueba
        self.authe.delete_user_account(self.user['idToken'])
        
        # Limpiar archivos temporales
        if os.path.exists(self.temp_dir):
            shutil.rmtree(self.temp_dir)

    # Test inicio de sesión con éxito   
    def test_postsignIn_success(self):
        # Simular el envío del formulario de inicio de sesión
        response = self.client.post(reverse('postsignIn'), {
            'email': self.email,
            'pass': 'A123.no'
        })
        
        # Verificar que la respuesta redirige a la página de inicio
        self.assertEqual(response.status_code, 200)
        self.assertTemplateUsed(response, 'Home.html')
        
    def test_postsignIn_invalid_credentials(self):
        # Simular el envío del formulario de inicio de sesión con credenciales incorrectas
        response = self.client.post(reverse('postsignIn'), {
            'email': self.email,
            'pass': 'wrongpassword'
        })
        
        # Verificar que la respuesta redirige a la página de inicio de sesión con un mensaje de error
        self.assertEqual(response.status_code, 200)
        self.assertTemplateUsed(response, 'Login.html')
        self.assertEqual(response.context['message'], 'Credenciales incorrectas')
        
    def test_upload_file_invalid_file(self):
        # Crear un archivo txt de prueba
        uploaded_file = SimpleUploadedFile('test.txt', b'This is a test file.', content_type='text/plain')
        response = self.client.post(reverse('upload_file'), {
            'archivo': uploaded_file,
            'nombre_modelo': 'test_model'
        })
        
        # Verificar que la respuesta redirige a la página de entrenamiento ya que ha fallado
        self.assertTemplateUsed(response, 'Entrenamiento.html')
        self.assertContains(response, 'El archivo no es un archivo ZIP. Por favor, sube un archivo ZIP válido.')


    def crear_modelo(self):
        # Método auxiliar para crear un modelo
        modelo_data = {
            "nombre": "modelo_test",
            "modelo": "model_test.tflite",
            "label": ["clase1", "clase2"]
        }
        user_uid = self.user['localId']
        
        # Añadir modelo directamente a la base de datos de Firebase
        referencia_modelo = self.db.child("usuarios").child(user_uid).child("modelos").push(modelo_data)
        clave_modelo = referencia_modelo["name"]
        
        # Subir un archivo ficticio a Firebase Storage
        archivo_local = os.path.join(self.temp_dir, 'model_test.tflite')
        with open(archivo_local, 'wb') as f:
            f.write(b'This is a test tflite model file.')
        
        archivo_remoto = f"usuarios/{user_uid}/{clave_modelo}/model.tflite"
        self.storage.child(archivo_remoto).put(archivo_local)
        
        return clave_modelo
    
    # Test para ver y borrar modelos
    def test_borrar_modelo(self):

        clave_modelo = self.crear_modelo()
        user_uid = self.user['localId']
        
        # Verificar que el modelo fue añadido correctamente
        modelo_guardado = self.db.child("usuarios").child(user_uid).child("modelos").child(clave_modelo).get().val()
        self.assertIsNotNone(modelo_guardado)
        self.assertEqual(modelo_guardado['nombre'], "modelo_test")


        response = self.client.get(reverse('modelos'))
        
        # Verificar que la respuesta contiene la lista de modelos
        self.assertEqual(response.status_code, 200)
        self.assertTemplateUsed(response, 'Modelos.html')
        self.assertIn('modelos_usuario', response.context)
        
        # Borrar el modelo
        response = self.client.get(reverse('borrar_modelo', args=[clave_modelo]))
        
        # Verificar que el modelo fue borrado correctamente
        modelo_borrado = self.db.child("usuarios").child(user_uid).child("modelos").child(clave_modelo).get().val()
        self.assertIsNone(modelo_borrado)

