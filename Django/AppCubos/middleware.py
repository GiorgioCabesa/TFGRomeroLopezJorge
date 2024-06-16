from django.shortcuts import redirect
from django.urls import reverse

class CheckLoginMiddleware:
    def __init__(self, get_response):
        self.get_response = get_response

    def __call__(self, request):
        # List of URLs that don't require login
        exempt_urls = [
            reverse('signIn'),
            reverse('signup'),
            reverse('postsignIn'),
            reverse('postsignUp'),
            reverse('reset'),
            reverse('postReset'),
        ]

        # Check if the user is not logged in and trying to access a restricted page
        if not request.session.get('uid') and request.path not in exempt_urls:
            return redirect('signIn')

        response = self.get_response(request)
        return response