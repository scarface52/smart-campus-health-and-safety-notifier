<h1>Smart Campus Health and Safety Notifier</h1>
A mobile application designed to report, track, and monitor health, safety, and maintenance incidents within a campus environment.

<h2>Features</h2>
<h3>Incident Management</h3>
<ul>
  <li><strong>Report Incidents:</strong> Create new notifications with titles, descriptions, locations, and photos.</li>
  <li><strong>Incident Feed:</strong> View a chronologically sorted list of incidents with filtering by type (Health, Safety, maintenance, etc.) and status.</li>
  <li><strong>Map View:</strong> Visualize incident locations and types on an interactive campus map.</li>
  <li><strong>Search:</strong> Search for specific incidents by keywords.</li>
  <li><strong>Follow/Notify:</strong> Follow specific incidents or subscribe to incident types to receive updates.</li>
</ul>
<h3>User Account</h3>
<ul>
  <li><strong>Authentication:</strong> Secure login, registration, and password reset functionality.</li>
  <li><strong>Profile Management:</strong> Update personal information and notification preferences.</li>
  <li><strong>Role-Based Access:</strong> Support for different user roles and faculties.</li>
</ul>
<h2>Tech Stack</h2>
<ul>
  <li><strong>Platform:</strong> Android (Kotlin)</li>
  <li><strong>Backend:</strong> Supabase (Authentication, Postgrest Database, Storage)</li>
  <li><strong>Networking:</strong> Ktor Client</li>
  <li><strong>Maps & Location:</strong> Google Maps SDK and Google Play Services Location</li>
</ul>
<h2>Project Structure</h2>
<ul>
  <li><strong>app/src/main/java:</strong> Kotlin source code including activities, fragments, and adapters.</li>
  <li><strong>app/src/main/res:</strong> UI layouts, strings, and navigation graphs.</li>
  <li><strong>app/docs:</strong> Database requirements and ER diagrams.</li>
</ul>
<h3>Configuration</h3>
<p>To get the project running, you must configure the following:</p>
<ol>
  <li><strong>Supabase Credentials:</strong> Update the DATABASE_URL and anon key in app/src/main/java/com/theretros/smartcampus/data/Constants.kt.</li>
  <li><strong>Google Maps API Key:</strong> Replace the null value in app/src/main/AndroidManifest.xml with your valid Google Maps API key.</li>
</ol>
<h3>Permissions</h3>
<p>The app requires the following permissions to function:</p>
<ul>
  <li><strong>INTERNET:</strong> For communicating with the Supabase backend.</li>
  <li><strong>ACCESS_FINE_LOCATION & ACCESS_COARSE_LOCATION:</strong> Required for map functionality and tagging incident locations.</li>
</ul>
<h3>Development Requirements</h3>
<ul>
  <li><strong>Min SDK:</strong> 26</li>
  <li><strong>Target/Compile SDK:</strong> 36</li>
  <li><strong>Java Version:</strong> 11</li>
</ul>
