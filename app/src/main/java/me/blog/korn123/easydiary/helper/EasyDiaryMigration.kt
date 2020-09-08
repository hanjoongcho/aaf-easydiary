package me.blog.korn123.easydiary.helper

import io.github.aafactory.commons.utils.DateUtils
import io.realm.DynamicRealm
import io.realm.FieldAttribute
import io.realm.RealmMigration
import io.realm.RealmObjectSchema
import me.blog.korn123.easydiary.models.Alarm
import me.blog.korn123.easydiary.models.Location

/**
 * Created by hanjoong on 2017-03-25.
 */

class EasyDiaryMigration : RealmMigration {
    
    override fun migrate(realm: DynamicRealm, oldVersion: Long, newVersion: Long) {
        var currentVersion = oldVersion
        val schema = realm.schema
        val diarySchema: RealmObjectSchema? = schema.get("DiaryDto")
        diarySchema?.let {
            if (currentVersion == 1L) {
                /* Date 필드는 추후 필요 시 마이그레이션 예정임 */
                //            diarySchema
                //                    .addField("date", Date.class)
                //                    .transform(new RealmObjectSchema.Function() {
                //                        @Override
                //                        public void apply(DynamicRealmObject obj) {
                //                            Date temp = new Date(obj.getLong("currentTimeMillis"));
                //                            obj.set("date", temp);
                //                        }
                //                    });

                it.addField("dateString", String::class.java)
                .transform { obj ->
                    val dateString = DateUtils.timeMillisToDateTime(obj.getLong("currentTimeMillis"), DateUtils.DATE_PATTERN_DASH)
                    obj.set("dateString", dateString)
                }

                currentVersion++
            }

            if (currentVersion == 2L) {
                it.addField("weather", Int::class.java)
                .transform { obj -> obj.set("weather", 0) }
                currentVersion++
            }

            if (currentVersion == 3L) {
                val photoUriSchema = schema.create("PhotoUriDto").addField("photoUri", String::class.java)

                it.addRealmListField("photoUris", photoUriSchema)
                .transform {
                    // obj.set("photoUris", null);
                }
                currentVersion++
            }

            if (currentVersion == 4L) {
                it.addField("fontName", String::class.java)
                currentVersion++
            }

            if (currentVersion == 5L) {
                it.addField("fontSize", Float::class.java)
                currentVersion++
            }
            
            if (currentVersion == 6L) {
                it.addField("isAllDay", Boolean::class.java)
                currentVersion++
            }

            if (currentVersion == 7L) {
                schema.create("Alarm")
                        .addField("sequence", Int::class.java, FieldAttribute.PRIMARY_KEY)
                        .addField("id", Int::class.java)
                        .addField("timeInMinutes", Int::class.java)
                        .addField("days", Int::class.java)
                        .addField("isEnabled", Boolean::class.java)
                        .addField("vibrate", Boolean::class.java)
                        .addField("soundTitle", String::class.java)
                        .addField("soundUri", String::class.java)
                        .addField("label", String::class.java)
                currentVersion++
            }

            if (currentVersion == 8L) {
                schema.get("Alarm")?.removeField("id")
                currentVersion++
            }

            if (currentVersion == 9L) {
                it.addField("isEncrypt", Boolean::class.java)
                currentVersion++
            }

            if (currentVersion == 10L) {
                it.addField("encryptKeyHash", String::class.java)
                currentVersion++
            }

            if (currentVersion == 11L) {
                schema.get("PhotoUriDto")?.let { photoUriDto ->
                    photoUriDto
                            .addField("mimeType", String::class.java)
                            .transform { obj -> obj.set("mimeType", MIME_TYPE_JPEG) }
                }
                currentVersion++
            }

            if (currentVersion == 12L) {
                it.addField("isSelected", Boolean::class.java)
                currentVersion++
            }

            if (currentVersion == 13L) {
                schema.get("Alarm")?.addField("workMode", Int::class.java)
                currentVersion++
            }

            if (currentVersion == 14L) {
                // Delete all test data
                realm.delete("Alarm")
                currentVersion++
            }

            if (currentVersion == 15L) {
                schema.create("ActionLog")
                        .addField("sequence", Int::class.java, FieldAttribute.PRIMARY_KEY)
                        .addField("className", String::class.java)
                        .addField("signature", String::class.java)
                        .addField("key", String::class.java)
                        .addField("value", String::class.java)
                currentVersion++
            }

            if (currentVersion == 16L) {
                currentVersion++
            }

            if (currentVersion == 17L) {
                schema.get("Alarm")?.addField("retryCount", Int::class.java)
                currentVersion++
            }

            if (currentVersion == 18L) {
                val location = schema.create("Location")
                        .addField("address", String::class.java)
                        .addField("latitude", Double::class.java)
                        .addField("longitude", Double::class.java)
                it.addRealmObjectField("location", location)
                currentVersion++
            }

            //        // During a migration, a DynamicRealm is exposed. A DynamicRealm is an untyped variant of a normal Realm, but
            //        // with the same object creation and query capabilities.
            //        // A DynamicRealm uses Strings instead of Class references because the Classes might not even exist or have been
            //        // renamed.
            //
            //        // Access the Realm schema in order to create, modify or delete classes and their fields.
            //        RealmSchema schema = realm.getSchema();
            //
            //        /************************************************
            //         // Version 0
            //         class Person
            //         @Required
            //         String firstName;
            //         @Required
            //         String lastName;
            //         int    age;
            //         // Version 1
            //         class Person
            //         @Required
            //         String fullName;            // combine firstName and lastName into single field.
            //         int age;
            //         ************************************************/
            //        // Migrate from version 0 to version 1
            //        if (mCurrentVersion == 0) {
            //            RealmObjectSchema personSchema = schema.get("Person");
            //
            //            // Combine 'firstName' and 'lastName' in a new field called 'fullName'
            //            personSchema
            //                    .addField("fullName", String.class, FieldAttribute.REQUIRED)
            //                    .transform(new RealmObjectSchema.Function() {
            //                        @Override
            //                        public void apply(DynamicRealmObject obj) {
            //                            obj.set("fullName", obj.getString("firstName") + " " + obj.getString("lastName"));
            //                        }
            //                    })
            //                    .removeField("firstName")
            //                    .removeField("lastName");
            //            mCurrentVersion++;
            //        }
            //
            //        /************************************************
            //         // Version 2
            //         class Pet                   // add a new model class
            //         @Required
            //         String name;
            //         @Required
            //         String type;
            //         class Person
            //         @Required
            //         String fullName;
            //         int age;
            //         RealmList<Pet> pets;    // add an array property
            //         ************************************************/
            //        // Migrate from version 1 to version 2
            //        if (mCurrentVersion == 1) {
            //
            //            // Create a new class
            //            RealmObjectSchema petSchema = schema.create("Pet")
            //                    .addField("name", String.class, FieldAttribute.REQUIRED)
            //                    .addField("type", String.class, FieldAttribute.REQUIRED);
            //
            //            // Add a new field to an old class and populate it with initial data
            //            schema.get("Person")
            //                    .addRealmListField("pets", petSchema)
            //                    .transform(new RealmObjectSchema.Function() {
            //                        @Override
            //                        public void apply(DynamicRealmObject obj) {
            //                            if (obj.getString("fullName").equals("JP McDonald")) {
            //                                DynamicRealmObject pet = realm.createObject("Pet");
            //                                pet.setString("name", "Jimbo");
            //                                pet.setString("type", "dog");
            //                                obj.getList("pets").add(pet);
            //                            }
            //                        }
            //                    });
            //            mCurrentVersion++;
            //        }
            //
            //        /************************************************
            //         // Version 3
            //         class Pet
            //         @Required
            //         String name;
            //         int type;               // type becomes int
            //         class Person
            //         String fullName;        // fullName is nullable now
            //         RealmList<Pet> pets;    // age and pets re-ordered (no action needed)
            //         int age;
            //         ************************************************/
            //        // Migrate from version 2 to version 3
            //        if (mCurrentVersion == 2) {
            //            RealmObjectSchema personSchema = schema.get("Person");
            //            personSchema.setNullable("fullName", true); // fullName is nullable now.
            //
            //            // Change type from String to int
            //            schema.get("Pet")
            //                    .addField("type_tmp", int.class)
            //                    .transform(new RealmObjectSchema.Function() {
            //                        @Override
            //                        public void apply(DynamicRealmObject obj) {
            //                            String oldType = obj.getString("type");
            //                            if (oldType.equals("dog")) {
            //                                obj.setLong("type_tmp", 1);
            //                            } else if (oldType.equals("cat")) {
            //                                obj.setInt("type_tmp", 2);
            //                            } else if (oldType.equals("hamster")) {
            //                                obj.setInt("type_tmp", 3);
            //                            }
            //                        }
            //                    })
            //                    .removeField("type")
            //                    .renameField("type_tmp", "type");
            //            mCurrentVersion++;
            //        }
        }
    }
}
