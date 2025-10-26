async function loadOAuthProviders() {
  try {
    const response = await fetch('/oauth2/providers');
    const providers = await response.json();

    const container = document.getElementById('oauth-buttons');

    providers.forEach(provider => {
      const button = createOAuthButton(provider);
      container.appendChild(button);
    });

  } catch (error) {
    console.error('Failed to load OAuth providers:', error);
  }
}

function createOAuthButton(provider) {
  const button = document.createElement('a');
  button.href = provider.auth_url;
  button.className = 'oauth-button flex justify-center items-center border-gray-700 rounded-lg p-2 border-1';

  button.innerHTML = `
    <div class="flex flex-row g-2">
      <img src="/image/${provider.id}-logo.svg" alt="${provider.name}" class="social-logo"/>
      <p class="text-body-medium">${provider.id} 계정으로 시작하기</p>
    </div>
  `;

  return button;
}

document.addEventListener('DOMContentLoaded', loadOAuthProviders);