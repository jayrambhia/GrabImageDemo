GrabImageDemo
---

### Design

This app is designed based on MVP architecture. Activities are considered as Views. Presenters load
data from Models and present to the View. Views are made passive and most of the logic is implemented
in Presenters and Models. Database Manager, Services and other utilities don't come under Model or
Presenter and hence are kept separately as Controllers.

Each Activity may implement a View(interface). Since Activity is the entry point in android, Presenters
have to be injected or initialized in Activity class.

Eg. ImageListActivity implements ImageListView with methods

 - setData()
 - getContext()
 - createActivityForResult() (This method is used for navigation)
 - showSnackBar()

There is not much logic involved in Activity classes except initializing views and setting data.

ImageListPresenterImpl which implements ImageListPresenter presents data to ImageListView.
ImageListActivity can be easily replaced with some other activity which would help in automated testing.

ImageListPresenter has following methods

 - attachView()
 - restoreInstance()
 - saveInstance()
 - detachView()

 - feedOldEntries()

 - onCameraImageRequested()
 - onCameraImageCaptured()
 - onImageUploaded()

onCameraImageRequested() is called when the user clicks on camera button. It's up to presenter how it
wants to reply to the request. It may deny the request if the user does not have appropriate permissions.
It may provide dummy image (for testing), or call some activity which provides image.

onCameraImageCaptured() and onImageUploaded() are used because navigation results are available with Activity.


### Database

I have used Android's SQLite database with the use of OrmLite. OrmLite provides easy access to the
database and we can skip on a lot of boilerplate code. There are few other ORM available for android,
but I have used OrmLite due to my familiarity with it.

ImageModel is the class which I am using to persist data in the database. Table name is defined as
`@DatabaseTable(tableName = "image_model")`. Following fields are added to the class/table.

 - id
 - address
 - latitude
 - longitude
 - created_ts (Time - image was created on the device)
 - updated_ts (Time - image was uploaded to the server)
 - mmpath_local (path of the image saved in local database)
 - mmpath_remote (URL of the image)
 - mmsize (size of the image. Not used.)

id is auto generated as of now (based on timestamp).

We may put other fields as user_id (id of the user who uploaded), group_id (id of group or channel
where the image was uploaded), etc. OrmLite also makes it easy to update database table if some fields
are changed.

`ImageModelProvider` class is used to CRUD data from database.

### Providers

####Image
Since the app involves loading of images, it is necessary to keep cache of bitmaps and load resized
bitmaps. Camera would generally save 8MP image and there is no need to load image of 8MP on 720p screen.
To resize the bitmap, we need to load the bitmap first. But loading an 8MP image would take a toll on RAM
and memory. Hence I am loading resized images and check if it needs more resizing or not. All of this
is done in `ImageProvider` class. It keeps an LRUCache of Bitmaps with key as local file path.
Whenever an image is requested, it checks for the bitmap is cache. If it is not available, it will
load from the disk, resize, add in the cache and load to the imageview. I/O needs to be done on the
background thread to avoid lagging UI. RxJava provides easy implementation with Observable and Subscribers
to do that and hence I have used it. This is just a basic usage of RxJava and it can be used to simplify
a lot of other things. This ImageProvider can be easily extended to work with Remote URLs.

####Feeder
`ImageFeeder` provides `ImageModelWrapper` which are used as wrappers over `ImageModel` for easy access.
Since it loads data from database, it is preferred that loading be done in background thread. But this
loaded data has to be put in the adapter on UI thread, otherwise RecyclerView will throw errors. It
becomes cumbersome to manage threads and calling handlers from thread, hence I have again used RxJava
to perform tasks in background and observe on main thread.

### Services

Location is very important for this application and hence I decided to use Service to get location.
As soon as the user starts the app, `LocationProvideService` is started. Since accurate location is
required, I am using GPS and Network both to get locations. If the user is indoors, it is very difficult
to get location by using GPS where as Network based location is easily available.

Last known location is obtained from both the providers. If the timestamp on the acquired location is
more than two minutes, the location is discarded.

`EventBus` is used for communication. Whenever location update is available, it is posted using EventBus.

### UI

ImageListActivity lists all the images that are uploaded by the user. Initially only few images are
provided to the user. As the user scrolls down, more images are loaded and provided. RecyclerView is
used to present the images. I like Material Design and hence I have used `CoordinatorLayout` to
handle view animations. I have added a floating action button to capture and upload the images. As it
was mentioned in the assignment that an action has to be added in action bar, I have done so using menus.
As the user clicks on it, Default Camera Intent is opened. Once the user takes a picture, the app
navigates to `ImageUploadActivity`.

ImageUploadActivity loads the clicked image and asks `LocationProviderService` to provide location. It
immediately provides last known location. Everything is actually done by `ImageUploadPresenterImpl`.
It will check for the accuracy and timestamp of the location provided by the service. If it checks
all the requirements, it is shown on the screen and update button becomes active. If the location is
inaccurate or old, it will wait for service to provide location. Once the user pressed Update button,
presenter shows a processing dialog and waits for 5 seconds (API is not present). After 5 seconds, it
will generate a random number between 0 to 4. If it is 0, it will show the user that update has failed.
Otherwise, it will add the image entry to the database and return result to `ImageListActivity`.

The app can be used in portrait and landscape mode and hence configuration changes are handles by saving
data in savedInstanceState. Since portrait layout for `ImageUploadActivity` doesn't work in landscape mode,
I have added another layout for landscape.
