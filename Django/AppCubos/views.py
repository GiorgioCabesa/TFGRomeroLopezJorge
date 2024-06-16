import zipfile  # Importa la biblioteca zipfile para manejar archivos ZIP
import os  # Importa la biblioteca os para interactuar con el sistema operativo
import re # Importa la bilioteca para manejar expresiones regulares
import random  # Importa la biblioteca random para generar valores aleatorios
import shutil  # Importa la biblioteca shutil para mover y copiar archivos
import json
import tensorflow as tf  # Importa TensorFlow para el desarrollo de modelos de aprendizaje automático
import matplotlib.pyplot as plt  # Importa Matplotlib para visualización de datos
from tensorflow.keras.preprocessing import image_dataset_from_directory  # Importa para crear conjuntos de datos de imágenes desde directorios
from tensorflow.keras.applications import MobileNetV2  # Importa MobileNetV2, un modelo preentrenado
from tensorflow.keras.layers import Dense, GlobalAveragePooling2D  # Importa capas para la construcción de modelos
from tensorflow.keras.models import Model  # Importa la clase Model para crear modelos personalizados
from tensorflow.keras.optimizers import Adam  # Importa el optimizador Adam para entrenar modelos
from cryptography.fernet import Fernet # Encriptacion

from django.shortcuts import render  # Importa render para renderizar plantillas
from django.shortcuts import redirect  # Importa redirect para redireccionar URLs
from django.http import FileResponse, HttpResponseNotFound, HttpResponse  # Importa diferentes tipos de respuestas HTTP
from django.template import Template, Context  # Importa Template y Context para trabajar con plantillas de Django
from django.contrib import messages  # Importa messages para manejar mensajes flash en Django

import pyrebase  # Importa Pyrebase para interactuar con Firebase
from django.conf import settings  # Importa settings para acceder a configuraciones de Django


config={
   
  "apiKey": "AIzaSyDRzAi6Ndx-jamSUh1JdmNMWoZ599gIs_c",

  "authDomain": "tensorcube-ec4ef.firebaseapp.com",

  "projectId": "tensorcube-ec4ef",

  "storageBucket": "tensorcube-ec4ef.appspot.com",

  "messagingSenderId": "95309594224",

  "appId": "1:95309594224:web:ce71cebea298865a853eb5",

  "databaseURL" : "https://tensorcube-ec4ef-default-rtdb.europe-west1.firebasedatabase.app"
}

# Inicizalizar herramientas Firebase
firebase=pyrebase.initialize_app(config)
authe = firebase.auth()
db = firebase.database()
storage = firebase.storage()



# Vista para iniciar sesión
def signIn(request):
    if 'uid' in request.session:
        del request.session['uid']
    success_message = request.session.pop('success_message', None)
    return render(request, "Login.html", {"success_message": success_message})

# Vista para la página principal
def home(request):
    return render(request,"Home.html")
 

# Vista para el inicio de sesion
def postsignIn(request):
    email = request.POST.get('email')
    pasw = request.POST.get('pass')
    try:
        # Método para el inicio de sesión con firebase
        user = authe.sign_in_with_email_and_password(email, pasw)
    except:
        message = "Credenciales incorrectas"
        return render(request, "Login.html", {"error_message": message})
    session_id = user['idToken']
    user_uid = user['localId']
    
    request.session['uid'] = str(session_id)
    request.session['uid_user'] = str(user_uid)

    return render(request, "Home.html")


# Vista para manejar el cierre de sesión
def logout(request):
    try:
        del request.session['uid']
    except:
        pass
    return render(request,"Login.html")

# Vista para manejar el registro de sesión
def signUp(request):
    return render(request,"Registration.html")

# Vista para manejar el registro de usuario
def postsignUp(request):
    email = request.POST.get('email')
    passs = request.POST.get('pass')
    confirm_password = request.POST.get('pass-repeat')

    # Las contraseñas deben coincidir
    if passs != confirm_password:
        message = "Las contraseñas no coinciden"
        return render(request, "Registration.html", {"message": message})

    # Validamos la contraseña para que sea robusta
    password_error = validate_password(passs)
    if password_error:
        return render(request, "Registration.html", {"message": password_error})

    try:
        # Metodo para registrar el usuario en firebase
        authe.create_user_with_email_and_password(email, passs)
        success_message = "Registro exitoso. Ahora puedes iniciar sesión."
        return render(request, "Login.html", {"success_message": success_message})
    except:
        message = "Error al crear la cuenta. Verifique su información e intente de nuevo."
        return render(request, "Registration.html", {"message": message})


# Función para validar la contraseña
def validate_password(password):
    if len(password) < 6:
        return "La contraseña debe tener al menos 6 caracteres"
    if not re.search(r'[A-Z]', password):
        return "La contraseña debe contener al menos una letra mayúscula"
    if not re.search(r'[0-9]', password):
        return "La contraseña debe contener al menos un número"
    if not re.search(r'[!@#$%^&*(),.?":{}|<>]', password):
        return "La contraseña debe contener al menos un símbolo"
    return None

# Vista para el reset de contraseña
def reset(request):
    return render(request, "Reset.html")

# Vista manejar el reset de contraseña
def postReset(request):
    email = request.POST.get('email')
    try:
        # Método para enviar al email para recuperar la contraseña
        authe.send_password_reset_email(email)
        message  = "Si su correo se encuentra registrado le llegará un email para recuperar su contraseña"
        print(message)
        return render(request, "Login.html", {"message":message})
    except:
        message  = "Error: Tu email no se encuentra registrado"
        print(message)
        return render(request, "Login.html", {"message":message})

# Vista para el entrenamiento
def entrena(request):
    contexto = {
        'media_url': settings.MEDIA_URL
    }
    return render(request,"Entrenamiento.html", contexto)


# Vista para subir y entrenar la red neuronal
def upload_file(request):
    if request.method == 'POST':
        archivo = request.FILES['archivo']
        nombre_modelo = request.POST.get('nombre_modelo')
        epochs = int(request.POST.get('epochs', 5))
       
        if archivo.name.endswith('.zip'):
            with open(os.path.join(settings.MEDIA_ROOT, archivo.name), 'wb+') as destination:
                for chunk in archivo.chunks():
                    destination.write(chunk)
            result = unzip_and_move_images(os.path.join(settings.MEDIA_ROOT, archivo.name), request.session['uid_user'], nombre_modelo, epochs)
            if result == "success":
                messages.success(request, 'Modelo creado con éxito.')
            else:
                messages.error(request, 'Error al crear el modelo: ' + result)
            return redirect('entrenamiento')
        else:
            messages.error(request, 'El archivo no es un archivo ZIP. Por favor, sube un archivo ZIP válido.')
    return render(request, 'Entrenamiento.html')


# Funcion que realiza toda la lógica para el entrenamiento y la subida de los modelos a la base de datos
def unzip_and_move_images(zip_file, user_uid, nombre_modelo, epochs):
   
    # Creamos una carpeta donde trabajaremos 
    random_folder_name = os.path.join(settings.MEDIA_ROOT, ''.join(random.choice('abcdefghijklmnopqrstuvwxyz') for _ in range(10)))
    os.makedirs(random_folder_name)
    moved_zip_file = os.path.join(random_folder_name, os.path.basename(zip_file))
    shutil.move(zip_file, moved_zip_file)
    try:
        with zipfile.ZipFile(moved_zip_file, 'r') as zip_ref:
            zip_ref.extractall(random_folder_name)
        classes = [f for f in os.listdir(random_folder_name) if os.path.isdir(os.path.join(random_folder_name, f))]
       
        if len(classes) < 2:
            return "Debe haber más de dos carpetas para poder ejecutar el programa."
        random_folder_name = random_folder_name.replace("\\", "/")
        path_train = os.path.join(random_folder_name, "train")
        path_val = os.path.join(random_folder_name, "val")
        
        # Recorremos todas las imagenes y lo repartimos en train y val
        for folder in classes:
            os.makedirs(os.path.join(random_folder_name, 'train', folder))
            os.makedirs(os.path.join(random_folder_name, 'val', folder))
            images = [f for f in os.listdir(os.path.join(random_folder_name, folder))]
            num_images = len(images)
            num_train = int(0.8 * num_images)
            random.shuffle(images)
            train_images = images[:num_train]
            val_images = images[num_train:]
           
            for image in train_images:
                source = os.path.join(random_folder_name, folder, image)
                destination = os.path.join(random_folder_name, 'train', folder, image)
                shutil.move(source, destination)
            for image in val_images:
                source = os.path.join(random_folder_name, folder, image)
                destination = os.path.join(random_folder_name, 'val', folder, image)
                shutil.move(source, destination)
       

        IMAGE_SIZE = (224, 224) # Pixeles de alto y ancho de la imagen
        BATCH_SIZE = 16 # Numero de imagenes que se entrenan a la vez
        EPOCHS = epochs # Número de iteraciones completas sobre el dataset que realiza el entrenamiento
        
        # Reparto del dataset a la red neuronal
        train_ds = image_dataset_from_directory(
            path_train,
            image_size=IMAGE_SIZE,
            batch_size=BATCH_SIZE
        )
        val_ds = image_dataset_from_directory(
            path_val,
            image_size=IMAGE_SIZE,
            batch_size=BATCH_SIZE
        )


        num_classes = len(train_ds.class_names) # Número de categorías a clasificar

        # Modelo base al que realizamos fine-tuning
        base_model = MobileNetV2(weights='imagenet', include_top=False, input_shape=(224, 224, 3))
        base_model.trainable = False

        # Configuración del fine-tuning
        x = base_model.output
        x = GlobalAveragePooling2D()(x)
        x = Dense(128, activation='relu')(x)
       
  
        predictions = Dense(num_classes, activation='softmax')(x)
        loss = 'sparse_categorical_crossentropy'
        metrics = ['accuracy']
        
        # Compilación y entrenamiento de la red neuronal
        model = Model(inputs=base_model.input, outputs=predictions)
        model.compile(optimizer=Adam(), loss=loss, metrics=metrics)
        model.fit(
            train_ds,
            epochs=EPOCHS,
            validation_data=val_ds
        )
        
        # Guardamos el modelo y lo convertimos a TFlite para dispositivos móviles
        model_save_path = os.path.join(random_folder_name, 'model_saved')
        model.save(model_save_path)
        converter = tf.lite.TFLiteConverter.from_saved_model(model_save_path)
        tflite_model = converter.convert()
        
        with open(os.path.join(random_folder_name, "model.tflite"), 'wb') as f:
            f.write(tflite_model)
        usuario_existente = db.child("usuarios").child(user_uid).get().val()
        
        # Comprobaciones para insertar en la base de datos firebase
        if usuario_existente is None:
            nuevo_usuario = {
                "modelos": {}
            }
            db.child("usuarios").child(user_uid).set(nuevo_usuario)
            print("Nuevo usuario creado.")
        else:
            modelos_usuario = usuario_existente.get("modelos", {})
            nuevo_usuario = {"modelos": modelos_usuario}
        
        # Creamos el nuevo modelo
        nuevo_modelo = {
            "nombre": nombre_modelo,
            "modelo": "model.tflite",
            "label": classes
        }
        # Insertamos en el JSON de nuestra base de datos el modelo
        referencia_modelo = db.child("usuarios").child(user_uid).child("modelos").push(nuevo_modelo)
        clave_modelo = referencia_modelo["name"]
        carpeta_usuario = f"{user_uid}/"
        
        # Creamos la carpeta del usuario donde se guardan los modelos
        try:
            storage.child(carpeta_usuario).get_url(None)
            print("Carpeta modelo creada con éxito")
       
        except Exception as e:
            print("La carpeta del usuario no existe en Storage. Creando carpeta...")
            storage.child(carpeta_usuario).put('', None)

        # Lógica para guardar el archivo model.tflite al storage de firebase (Antes solo habiamos guardado en el JSON la referencia al modelo)
        archivo_local = f"{random_folder_name}/model.tflite"
        print("Mi modelo " + archivo_local)
        archivo_remoto = f"usuarios/{user_uid}/{clave_modelo}/model.tflite"
        storage.child(archivo_remoto).put(archivo_local)
        db.child("usuarios").child(user_uid).child("modelos").update({clave_modelo: nuevo_modelo})
        download_url = storage.child(archivo_remoto).get_url(None)
        print("Enlace: " + download_url)
        return "success"
    except Exception as e:
        print(f"Se produjo un error")
        return str(e)
    finally:
        # Borramos la carpeta generada
        shutil.rmtree(random_folder_name)
        
        
# Vista para ver los modelos entrenados por el usuario
def ver_modelos(request):
    user_uid = request.session['uid_user']

    # Obtennemos los modelos del usuario
    modelos_usuario = obtener_modelos_usuario(request.session['uid_user'])
    if modelos_usuario:
        for clave, modelo in modelos_usuario.items():
            download_url = f"usuarios/{user_uid}/{clave}/model.tflite"
            modelo['archivo'] = storage.child(download_url).get_url(None)
            # Imprimir detalles del modelo para la depuración de testing
            print(f"Modelo {clave}: {modelo}")

        return render(request, "Modelos.html", {"modelos_usuario": modelos_usuario, "user_uid": user_uid})
    else:
        print("El usuario no tiene modelos.")
        return render(request, "Modelos.html", {"modelos_usuario": None})

# Metodo para obtener los modelos de un usuario
def obtener_modelos_usuario(user_uid):
    # Especifica la referencia a la ubicación de los modelos del usuario en la base de datos
    ref_modelos_usuario = db.child("usuarios").child(user_uid).child("modelos")
    # Obtén los modelos del usuario
    modelos_usuario = ref_modelos_usuario.get().val()
    if modelos_usuario:
        return modelos_usuario
    else:
        print("El usuario no tiene modelos.")
        return None

# Metodo para borrar un modelo
def borrar_modelo(request,clave):
    user_uid = request.session['uid_user']

    # Borrar en realtimedatabase
    ref_modelo = db.child("usuarios").child(user_uid).child("modelos").child(clave)
    ref_modelo.remove()


    # Eliminar el modelo del almacenamiento en Firebase Storage
    archivo_url = f"usuarios/{user_uid}/{clave}/model.tflite"
    storage.delete(archivo_url, request.session['uid'])

    return redirect('modelos')


def custom_404(request, exception):
    return render(request, 'AppCubos/404.html', status=404)

def custom_500(request):
    return render(request, 'AppCubos/500.html', status=500)
