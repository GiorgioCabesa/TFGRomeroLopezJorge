"""
URL configuration for cubosderubik project.

The `urlpatterns` list routes URLs to views. For more information please see:
    https://docs.djangoproject.com/en/4.2/topics/http/urls/
Examples:
Function views
    1. Add an import:  from my_app import views
    2. Add a URL to urlpatterns:  path('', views.home, name='home')
Class-based views
    1. Add an import:  from other_app.views import Home
    2. Add a URL to urlpatterns:  path('', Home.as_view(), name='home')
Including another URLconf
    1. Import the include() function: from django.urls import include, path
    2. Add a URL to urlpatterns:  path('blog/', include('blog.urls'))
"""
from django.contrib import admin
from django.urls import path
from AppCubos import views
from django.conf import settings
from django.conf.urls.static import static 
urlpatterns = [
    path('admin/', admin.site.urls),

     
     
    path('', views.signIn, name="signIn"),
    path('postsignIn/', views.postsignIn, name='postsignIn'),
    path('signUp/', views.signUp, name="signup"),
    path('logout/', views.logout, name="log"),
    path('postsignUp/', views.postsignUp, name="postsignUp"),
    path('reset/', views.reset, name="reset"),
    path('postReset/', views.postReset, name="postReset"),
    path('entrenamiento/', views.entrena, name="entrenamiento"),
    path('modelos/', views.ver_modelos, name="modelos"),
    path('upload/', views.upload_file, name="upload_file"),
    path('borrar_modelo/<str:clave>/', views.borrar_modelo, name='borrar_modelo'),
]


if settings.DEBUG:
    urlpatterns += static(settings.MEDIA_URL, document_root=settings.MEDIA_ROOT)



handler404 = 'AppCubos.views.custom_404'
handler500 = 'AppCubos.views.custom_500'